package galindo.raul.virtualclubs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import galindo.raul.virtualclubs.dtos.request.EmailRequest;
import galindo.raul.virtualclubs.repositories.UserTokenRepository;
import galindo.raul.virtualclubs.services.EmailService;
import galindo.raul.virtualclubs.services.GoogleAuthService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import galindo.raul.virtualclubs.util.IntegrationTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para la verificación de email (VC-20).
 *
 * Cubre:
 * - /register envía email de verificación
 * - GET /v1/auth/verify con token válido → redirect con status=1
 * - GET /v1/auth/verify con token inválido → redirect con status=0
 * - GET /v1/auth/verify con token ya consumido → redirect con status=0
 * - POST /v1/auth/requestVerify sin autenticación → 403
 * - POST /v1/auth/requestVerify autenticado → 200 + email enviado
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
@Transactional
class EmailVerificationIntegrationTest {

    private static final String BASE     = "/v1/auth";
    private static final String EMAIL    = "verify@test.com";
    private static final String PASSWORD = "PAss12";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserTokenRepository userTokenRepository;

    @MockitoBean private GoogleAuthService googleAuthService;
    @MockitoBean private EmailService emailService;

    // ─────────────────────────────────────────────────────────────
    // POST /v1/auth/register → debe enviar email de verificación
    // ─────────────────────────────────────────────────────────────

    @Test
    void register_enviaEmailDeVerificacion() throws Exception {
        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, PASSWORD)))
                .andExpect(status().isCreated());

        ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailService).sendEmail(captor.capture());

        EmailRequest emailRequest = captor.getValue();
        assertThat(emailRequest.to()).isEqualTo(EMAIL);
        assertThat(emailRequest.template()).isEqualTo("verify-email");

        // La URL de verificación debe contener el path correcto
        Object model = emailRequest.model();
        assertThat(model.toString()).contains("/v1/auth/verify?token=");
    }

    @Test
    void register_tokenGuardadoEnBD() throws Exception {
        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, PASSWORD)));

        assertThat(userTokenRepository.count()).isEqualTo(1);
        assertThat(userTokenRepository.findAll().getFirst().isConsumed()).isFalse();
    }

    // ─────────────────────────────────────────────────────────────
    // GET /v1/auth/verify
    // ─────────────────────────────────────────────────────────────

    @Test
    void verify_tokenValido_redirigeCon_status1() throws Exception {
        String verifyToken = registrarYObtenerVerifyToken();

        mockMvc.perform(get(BASE + "/verify").param("token", verifyToken))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    String location = result.getResponse().getHeader("Location");
                    assertThat(location).contains("status=1");
                });
    }

    @Test
    void verify_tokenValido_marcaEmailComoVerificado() throws Exception {
        String verifyToken = registrarYObtenerVerifyToken();

        mockMvc.perform(get(BASE + "/verify").param("token", verifyToken));

        // El token debe estar marcado como consumido
        assertThat(userTokenRepository.findAll())
                .allMatch(t -> t.isConsumed());
    }

    @Test
    void verify_tokenInvalido_redirigeCon_status0() throws Exception {
        mockMvc.perform(get(BASE + "/verify").param("token", "token-inexistente"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    String location = result.getResponse().getHeader("Location");
                    assertThat(location).contains("status=0");
                });
    }

    @Test
    void verify_tokenYaConsumido_redirigeCon_status0() throws Exception {
        String verifyToken = registrarYObtenerVerifyToken();

        // Primer uso: consume el token
        mockMvc.perform(get(BASE + "/verify").param("token", verifyToken));
        // Segundo uso: debe fallar
        mockMvc.perform(get(BASE + "/verify").param("token", verifyToken))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    String location = result.getResponse().getHeader("Location");
                    assertThat(location).contains("status=0");
                });
    }

    // ─────────────────────────────────────────────────────────────
    // POST /v1/auth/requestVerify
    // ─────────────────────────────────────────────────────────────

    @Test
    void requestVerify_sinAutenticacion_retorna403() throws Exception {
        mockMvc.perform(post(BASE + "/requestVerify"))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestVerify_autenticado_retorna200YEnviaEmail() throws Exception {
        // Primero registramos (capturamos token de registro pero no lo verificamos)
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(EMAIL, PASSWORD)));

        String accessToken = loginYObtenerAccessToken(EMAIL, PASSWORD);

        mockMvc.perform(post(BASE + "/requestVerify")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Se enviaron 2 emails: uno al registrar y otro al requestVerify
        ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailService, org.mockito.Mockito.times(2)).sendEmail(captor.capture());
        assertThat(captor.getAllValues())
                .allMatch(req -> "verify-email".equals(req.template()));
    }

    @Test
    void requestVerify_autenticado_reemplazaTokenAnterior() throws Exception {
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(EMAIL, PASSWORD)));

        // Hay 1 token en BD tras el registro
        assertThat(userTokenRepository.count()).isEqualTo(1);

        String accessToken = loginYObtenerAccessToken(EMAIL, PASSWORD);

        mockMvc.perform(post(BASE + "/requestVerify")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Sigue habiendo 1 token (el anterior fue reemplazado por el nuevo)
        assertThat(userTokenRepository.count()).isEqualTo(1);
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private String body(String email, String password) {
        return IntegrationTestUtils.authBody(email, password);
    }

    /**
     * Registra un usuario y devuelve el plaintext token de verificación
     * capturándolo del EmailRequest enviado al mock.
     */
    @SuppressWarnings("unchecked")
    private String registrarYObtenerVerifyToken() throws Exception {
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(EMAIL, PASSWORD)));

        ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailService).sendEmail(captor.capture());

        // El modelo es Map<String, Object> — extraemos verifyUrl directamente
        Map<String, Object> model = (Map<String, Object>) captor.getValue().model();
        String verifyUrl = (String) model.get("verifyUrl");

        // verifyUrl = "https://api-vc.rgal.dev/v1/auth/verify?token=<token>"
        String query = URLDecoder.decode(verifyUrl.substring(verifyUrl.indexOf('?') + 1), StandardCharsets.UTF_8);
        // query = "token=<plaintext>"
        return query.substring("token=".length());
    }

    private String loginYObtenerAccessToken(String email, String password) throws Exception {
        return IntegrationTestUtils.loginAndGetAccessToken(mockMvc, objectMapper, email, password);
    }
}