package galindo.raul.virtualclubs.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import galindo.raul.virtualclubs.repositories.RefreshTokenRepository;
import galindo.raul.virtualclubs.repositories.UserTokenRepository;
import galindo.raul.virtualclubs.repositories.VirtualClubsUsersDetailsRepository;
import galindo.raul.virtualclubs.services.GoogleAuthService;
import galindo.raul.virtualclubs.services.MailerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para AuthController.
 *
 * - Usa H2 en memoria (ver application-test.properties)
 * - @MockBean evita que GoogleAuthService haga llamadas HTTP reales al arrancar
 * - @MockBean evita que MailerService intente conectarse a un servidor SMTP
 * - WebEnvironment.MOCK usa MockMvc sin levantar un servidor real (ignora SSL)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
class AuthControllerIntegrationTest {

    private static final String BASE = "/api/auth";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // Estos beans se reemplazan por mocks — no hacen llamadas externas
    @MockBean private GoogleAuthService googleAuthService;
    @MockBean private MailerService mailerService;

    @Autowired private VirtualClubsUsersDetailsRepository userRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private UserTokenRepository userTokenRepository;

    @AfterEach
    void limpiarBD() {
        // Orden importante: las tablas hijas se borran antes que users (FK constraints)
        refreshTokenRepository.deleteAll();
        userTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/auth/register
    // ─────────────────────────────────────────────────────────────

    @Test
    void register_usuarioNuevo_retorna200() throws Exception {
        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "nuevo@test.com", "password": "Pass123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void register_usuarioDuplicado_retorna403() throws Exception {
        String body = """
                {"email": "dupe@test.com", "password": "Pass123"}
                """;

        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("user_already_exists"));
    }

    @Test
    void register_emailInvalido_retorna400() throws Exception {
        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "no-es-un-email", "password": "Pass123"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordCorta_retorna400() throws Exception {
        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "valido@test.com", "password": "abc"}
                                """))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/auth/authenticate
    // ─────────────────────────────────────────────────────────────

    @Test
    void authenticate_credencialesCorrectas_retornaTokens() throws Exception {
        registrar("auth@test.com", "Pass123");

        mockMvc.perform(post(BASE + "/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "auth@test.com", "password": "Pass123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void authenticate_contrasenaIncorrecta_retorna403() throws Exception {
        registrar("wrong@test.com", "Pass123");

        mockMvc.perform(post(BASE + "/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "wrong@test.com", "password": "Incorrect1"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("invalid_credentials"));
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/auth/refresh
    // ─────────────────────────────────────────────────────────────

    @Test
    void refresh_tokenValido_retornaNuevoAccessToken() throws Exception {
        registrar("refresh@test.com", "Pass123");
        String refreshToken = autenticarYObtenerRefreshToken("refresh@test.com", "Pass123");

        mockMvc.perform(post(BASE + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void refresh_tokenInvalido_retorna401() throws Exception {
        mockMvc.perform(post(BASE + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "esto-no-es-un-jwt-valido"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/auth/logout
    // ─────────────────────────────────────────────────────────────

    @Test
    void logout_siempreRetorna200() throws Exception {
        mockMvc.perform(post(BASE + "/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "cualquier-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void logout_tokenReal_eliminaTokenDeLaBD() throws Exception {
        registrar("logout@test.com", "Pass123");
        String refreshToken = autenticarYObtenerRefreshToken("logout@test.com", "Pass123");

        assertThat(refreshTokenRepository.count()).isEqualTo(1);

        // Después del logout el refresh token debe desaparecer de la BD
        mockMvc.perform(post(BASE + "/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isOk());

        assertThat(refreshTokenRepository.count()).isZero();
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/auth/request-password-reset
    // ─────────────────────────────────────────────────────────────

    @Test
    void requestPasswordReset_emailNoExiste_retorna200SinEnviarEmail() throws Exception {
        mockMvc.perform(post(BASE + "/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "nadie@test.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // No debe intentar enviar email si el usuario no existe
        verify(mailerService, never()).sendSimpleEmail(any(), any(), any());
    }

    @Test
    void requestPasswordReset_emailExiste_enviaMail() throws Exception {
        registrar("reset@test.com", "Pass123");

        mockMvc.perform(post(BASE + "/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "reset@test.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(mailerService).sendSimpleEmail(any(), any(), any());
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private void registrar(String email, String password) throws Exception {
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}"));
    }

    private String autenticarYObtenerRefreshToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post(BASE + "/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}"))
                .andReturn();

        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");

        return data.get("refreshToken").asText();
    }
}
