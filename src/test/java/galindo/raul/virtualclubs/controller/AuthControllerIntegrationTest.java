package galindo.raul.virtualclubs.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import galindo.raul.virtualclubs.models.entities.DeviceEntity;
import galindo.raul.virtualclubs.models.entities.RefreshTokenEntity;
import galindo.raul.virtualclubs.repositories.DeviceRepository;
import galindo.raul.virtualclubs.repositories.RefreshTokenRepository;
import galindo.raul.virtualclubs.repositories.UserEntityRepository;
import galindo.raul.virtualclubs.services.EmailService;
import galindo.raul.virtualclubs.services.GoogleAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import galindo.raul.virtualclubs.util.IntegrationTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para AuthController y JwtAuthLoginFilter.
 *
 * - Usa H2 en memoria (ver application-test.properties)
 * - @MockitoBean evita que GoogleAuthService haga llamadas HTTP reales al arrancar
 * - @MockitoBean evita que EmailService intente conectarse a un servidor SMTP
 * - WebEnvironment.MOCK usa MockMvc sin levantar un servidor real (ignora SSL)
 *
 * Formato de respuesta por endpoint:
 * - POST /v1/auth/login    → {"success":true, "data":{"accessToken":"...","refreshToken":"..."}} (ApiResponse — lo escribe JwtAuthLoginFilter)
 * - POST /v1/auth/register → {"success":true, "message":null, "data":{"accessToken":"...","refreshToken":"...","email":"..."}}
 * - POST /v1/auth/refresh  → {"success":true, "message":null, "data":{"accessToken":"...","refreshToken":"..."}}
 * - DELETE /v1/auth/logout → {"success":true, "message":null, "data":null}  (requiere Bearer token)
 *
 * Contraseña válida: mínimo 2 mayúsculas, 2 minúsculas, 1 dígito (@StrongPassword) y mínimo 6 caracteres (@Size).
 * Ejemplo usado en tests: "PAss12" (P,A mayúsculas | s,s minúsculas | 1,2 dígitos — cumple @StrongPassword y @Size(min=6)).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
@Transactional
class AuthControllerIntegrationTest {

    private static final String BASE    = "/v1/auth";
    private static final String EMAIL   = "test@test.com";
    private static final String PASSWORD = "PAss12"; // 2 upper, 2 lower, 2 digits — cumple @StrongPassword y @Size(min=6)

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // Estos beans se reemplazan por mocks — no hacen llamadas externas
    @MockitoBean private GoogleAuthService googleAuthService;
    @MockitoBean private EmailService emailService;

    @Autowired private UserEntityRepository userRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private DeviceRepository deviceRepository;

    // ─────────────────────────────────────────────────────────────
    // POST /v1/auth/register
    // ─────────────────────────────────────────────────────────────

    @Test
    void register_usuarioNuevo_retorna201() throws Exception {
        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.email").value(EMAIL));
    }

    @Test
    void register_usuarioDuplicado_retorna409() throws Exception {
        // Primer registro
        mockMvc.perform(post(BASE + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(EMAIL, PASSWORD)));

        // Segundo registro con el mismo email → 409 CONFLICT (UserAlreadyExistException)
        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_emailInvalido_retorna400() throws Exception {
        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("no-es-un-email", PASSWORD)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordDebil_retorna400() throws Exception {
        // "abc" → 0 mayúsculas, 0 dígitos → falla @StrongPassword
        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, "abc")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordDemasiadoCorto_retorna400() throws Exception {
        // "PAss1" → 5 chars → falla @Size(min=6)
        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, "PAss1")))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────────────────────────────────────────────
    // POST /v1/auth/login  (manejado por JwtAuthLoginFilter)
    // La respuesta está envuelta en ApiResponse:
    //   {"success":true, "data":{"accessToken":"...", "refreshToken":"..."}}
    // ─────────────────────────────────────────────────────────────

    @Test
    void login_credencialesCorrectas_retornaTokens() throws Exception {
        register(EMAIL, PASSWORD);

        mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void login_contrasenaIncorrecta_retorna401() throws Exception {
        register(EMAIL, PASSWORD);

        mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, "WRong9"))) // contraseña incorrecta
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // POST /v1/auth/refresh
    // ─────────────────────────────────────────────────────────────

    @Test
    void refresh_tokenValido_retornaNuevoAccessToken() throws Exception {
        register(EMAIL, PASSWORD);
        String refreshToken = loginYObtenerRefreshToken(EMAIL, PASSWORD);

        mockMvc.perform(post(BASE + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void refresh_soloEliminaTokenUsado_otroDispositivoSigueActivo() throws Exception {
        // Dispositivo A: login real → refresh token almacenado en BD
        register(EMAIL, PASSWORD);
        String refreshTokenA = loginYObtenerRefreshToken(EMAIL, PASSWORD);

        // Dispositivo B: insertar segundo token directamente en BD (simula otra sesión)
        galindo.raul.virtualclubs.models.entities.UserEntity user =
                userRepository.findByEmail(EMAIL).orElseThrow();
        DeviceEntity deviceB = deviceRepository.save(DeviceEntity.builder()
                .deviceId("device-b")
                .deviceName("Device B")
                .deviceType("TEST")
                .ipAddress("192.168.0.2")
                .user(user)
                .build());
        refreshTokenRepository.saveAndFlush(
                RefreshTokenEntity.builder()
                        .user(user)
                        .token("fake-hash-device-b-unique-token")
                        .device(deviceB)
                        .build());

        assertThat(refreshTokenRepository.count()).isEqualTo(2);

        // Refrescar con token del dispositivo A
        mockMvc.perform(post(BASE + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshTokenA + "\"}"))
                .andExpect(status().isOk());

        // El token del dispositivo B debe seguir activo — no fue eliminado por el refresh
        assertThat(refreshTokenRepository.findAll())
                .anyMatch(t -> "device-b".equals(t.getDevice().getDeviceId()) && !t.isRevoked());
    }

    @Test
    void refresh_tokenMalformado_retorna401() throws Exception {
        // "no-es-un-jwt" → JwtUtils.getUsernameFromToken lanza MalformedJwtException
        // GlobalExceptionHandler lo captura con @ExceptionHandler(JwtException.class) → 401
        mockMvc.perform(post(BASE + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"no-es-un-jwt\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(3));
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE /v1/auth/logout  (requiere Bearer token en Authorization)
    // ─────────────────────────────────────────────────────────────

    @Test
    void logout_sinAutenticacion_retorna403() throws Exception {
        mockMvc.perform(delete(BASE + "/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    void logout_conAuth_retorna200() throws Exception {
        register(EMAIL, PASSWORD);
        String accessToken = loginYObtenerAccessToken(EMAIL, PASSWORD);

        mockMvc.perform(delete(BASE + "/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void logout_tokenReal_marcaTokenComoRevocado() throws Exception {
        register(EMAIL, PASSWORD);
        String accessToken = loginYObtenerAccessToken(EMAIL, PASSWORD);

        assertThat(refreshTokenRepository.count()).isEqualTo(1);

        mockMvc.perform(delete(BASE + "/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // El logout NO elimina el registro: lo marca como revocado (revoked = true)
        assertThat(refreshTokenRepository.findAll())
                .allMatch(RefreshTokenEntity::isRevoked);
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers (delegados a IntegrationTestUtils)
    // ─────────────────────────────────────────────────────────────

    private String body(String email, String password) {
        return IntegrationTestUtils.authBody(email, password);
    }

    private void register(String email, String password) throws Exception {
        IntegrationTestUtils.register(mockMvc, email, password);
    }

    private String loginYObtenerRefreshToken(String email, String password) throws Exception {
        return IntegrationTestUtils.loginAndGetRefreshToken(mockMvc, objectMapper, email, password);
    }

    private String loginYObtenerAccessToken(String email, String password) throws Exception {
        return IntegrationTestUtils.loginAndGetAccessToken(mockMvc, objectMapper, email, password);
    }
}
