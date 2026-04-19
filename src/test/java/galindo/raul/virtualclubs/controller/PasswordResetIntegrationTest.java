package galindo.raul.virtualclubs.controller;

import tools.jackson.databind.ObjectMapper;
import galindo.raul.virtualclubs.dtos.request.EmailRequest;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.enums.TokenType;
import galindo.raul.virtualclubs.repositories.RefreshTokenRepository;
import galindo.raul.virtualclubs.repositories.UserEntityRepository;
import galindo.raul.virtualclubs.services.EmailService;
import galindo.raul.virtualclubs.services.GoogleAuthService;
import galindo.raul.virtualclubs.services.UserTokenService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para el flujo de reset de contraseña (VC-21).
 *
 * Cubre:
 * - POST /v1/auth/requestPasswordReset: siempre 200, envía email solo si existe
 * - GET /v1/auth/resetPasswordRedirect: redirige al deep link
 * - POST /v1/auth/resetPassword: válido, inválido, consumido, sin provider LOCAL
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
@Transactional
class PasswordResetIntegrationTest {

    private static final String BASE     = "/v1/auth";
    private static final String EMAIL    = "reset@test.com";
    private static final String PASSWORD = "PAss12";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserEntityRepository userRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private UserTokenService userTokenService;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockitoBean private GoogleAuthService googleAuthService;
    @MockitoBean private EmailService emailService;

    // ─────────────────────────────────────────────────────────────
    // POST /v1/auth/requestPasswordReset
    // ─────────────────────────────────────────────────────────────

    @Test
    void requestPasswordReset_emailExistente_retorna200() throws Exception {
        register(EMAIL, PASSWORD);

        mockMvc.perform(post(BASE + "/requestPasswordReset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + EMAIL + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void requestPasswordReset_emailInexistente_retorna200SinEnviarEmail() throws Exception {
        mockMvc.perform(post(BASE + "/requestPasswordReset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"noexiste@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // No debe enviar email — previene enumeración de usuarios (OWASP)
        verify(emailService, never()).sendEmail(any());
    }

    @Test
    void requestPasswordReset_emailExistente_enviaEmailConTemplate() throws Exception {
        register(EMAIL, PASSWORD);
        clearInvocations(emailService); // el register ya envió el email de verificación

        mockMvc.perform(post(BASE + "/requestPasswordReset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + EMAIL + "\"}"));

        ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailService).sendEmail(captor.capture());

        EmailRequest sent = captor.getValue();
        assertThat(sent.to()).isEqualTo(EMAIL);
        assertThat(sent.template()).isEqualTo("password-reset-email");

        @SuppressWarnings("unchecked")
        Map<String, Object> model = (Map<String, Object>) sent.model();
        assertThat(model.get("resetUrl").toString()).contains("/v1/auth/resetPasswordRedirect?token=");
        assertThat(model.get("expiryMinutes")).isNotNull();
    }

    @Test
    void requestPasswordReset_emailInvalido_retorna400() throws Exception {
        mockMvc.perform(post(BASE + "/requestPasswordReset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"no-es-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─────────────────────────────────────────────────────────────
    // GET /v1/auth/resetPasswordRedirect
    // ─────────────────────────────────────────────────────────────

    @Test
    void resetPasswordRedirect_redirigueAlDeepLink() throws Exception {
        mockMvc.perform(get(BASE + "/resetPasswordRedirect").param("token", "some-token"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    String location = result.getResponse().getHeader("Location");
                    assertThat(location).startsWith("virtualclubs://pass/resetPassword?token=");
                });
    }

    // ─────────────────────────────────────────────────────────────
    // POST /v1/auth/resetPassword
    // ─────────────────────────────────────────────────────────────

    @Test
    void resetPassword_tokenValido_retorna200() throws Exception {
        register(EMAIL, PASSWORD);
        String resetToken = crearTokenDeReset();

        mockMvc.perform(post(BASE + "/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetBody(resetToken, "NuevoPAss12")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void resetPassword_tokenValido_cambiaPassword() throws Exception {
        register(EMAIL, PASSWORD);
        String resetToken = crearTokenDeReset();

        mockMvc.perform(post(BASE + "/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetBody(resetToken, "NuevoPAss12")));

        // La nueva contraseña debe estar hasheada en BD
        UserEntity user = userRepository.findByEmail(EMAIL).orElseThrow();
        String nuevoHash = user.getAuthProviderEntities().stream()
                .filter(ap -> "LOCAL".equalsIgnoreCase(ap.getProviderName()))
                .findFirst()
                .orElseThrow()
                .getPasswordHash();
        assertThat(passwordEncoder.matches("NuevoPAss12", nuevoHash)).isTrue();
        assertThat(passwordEncoder.matches(PASSWORD, nuevoHash)).isFalse();
    }

    @Test
    void resetPassword_tokenValido_revocaTodasLasSesiones() throws Exception {
        register(EMAIL, PASSWORD);
        // El registro ya genera un refresh token
        assertThat(refreshTokenRepository.count()).isEqualTo(1);
        assertThat(refreshTokenRepository.findAll().getFirst().isRevoked()).isFalse();

        String resetToken = crearTokenDeReset();
        mockMvc.perform(post(BASE + "/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetBody(resetToken, "NuevoPAss12")));

        // Todas las sesiones deben quedar revocadas
        assertThat(refreshTokenRepository.findAll())
                .allMatch(t -> t.isRevoked());
    }

    @Test
    void resetPassword_tokenInvalido_retorna403() throws Exception {
        mockMvc.perform(post(BASE + "/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetBody("token-inexistente", "NuevoPAss12")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void resetPassword_tokenConsumido_retorna403() throws Exception {
        register(EMAIL, PASSWORD);
        String resetToken = crearTokenDeReset();

        // Primer uso
        mockMvc.perform(post(BASE + "/resetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetBody(resetToken, "NuevoPAss12")));

        // Segundo uso con el mismo token
        mockMvc.perform(post(BASE + "/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetBody(resetToken, "OtroPass12")))
                .andExpect(status().isForbidden());
    }

    @Test
    void resetPassword_passwordDebil_retorna400() throws Exception {
        register(EMAIL, PASSWORD);
        String resetToken = crearTokenDeReset();

        // "abc" falla @StrongPassword
        mockMvc.perform(post(BASE + "/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetBody(resetToken, "abc")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void resetPassword_passwordCorto_retorna400() throws Exception {
        register(EMAIL, PASSWORD);
        String resetToken = crearTokenDeReset();

        // "PAss1" tiene 5 chars — falla @Size(min=6)
        mockMvc.perform(post(BASE + "/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetBody(resetToken, "PAss1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}"));
    }

    private String crearTokenDeReset() {
        UserEntity user = userRepository.findByEmail(EMAIL).orElseThrow();
        return userTokenService.createTokenFor(user, TokenType.PASSWORD_RESET);
    }

    private String resetBody(String token, String newPassword) {
        return "{\"token\": \"" + token + "\", \"newPassword\": \"" + newPassword + "\"}";
    }
}