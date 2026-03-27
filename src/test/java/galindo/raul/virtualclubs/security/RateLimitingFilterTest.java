package galindo.raul.virtualclubs.security;

import galindo.raul.virtualclubs.services.GoogleAuthService;
import galindo.raul.virtualclubs.services.MailerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para RateLimitingFilter.
 *
 * Cada test usa una IP única vía X-Forwarded-For para evitar que los buckets
 * de un test interfieran con otro (los buckets persisten en el contexto de Spring).
 *
 * Límites configurados en application.properties:
 *   /v1/auth/login    → 5 peticiones/min
 *   /v1/auth/register → 3 peticiones/min
 */
// Contexto propio con límites reales — no comparte el contexto con AuthControllerIntegrationTest
// (application-test.properties tiene límites altos para esos tests; estos properties los sobreescriben)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "rate-limiting.limits[/v1/auth/login]=5",
        "rate-limiting.limits[/v1/auth/register]=3"
    }
)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
class RateLimitingFilterTest {

    private static final String LOGIN    = "/v1/auth/login";
    private static final String REGISTER = "/v1/auth/register";
    private static final String REFRESH  = "/v1/auth/refresh";

    @Autowired private MockMvc mockMvc;

    @MockitoBean private GoogleAuthService googleAuthService;
    @MockitoBean private MailerService mailerService;

    // Cuerpo mínimo válido para pasar el filtro (el contenido no importa para el test de rate limiting)
    private static final String BODY = "{\"email\":\"a@b.com\",\"password\":\"PAss12\"}";

    // ─────────────────────────────────────────────────────────────
    // /v1/auth/login — límite 5/min
    // ─────────────────────────────────────────────────────────────

    @Test
    void login_dentroDelLimite_noBloqueado() throws Exception {
        // 5 peticiones desde la misma IP → todas deben pasar el filtro (no 429)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post(LOGIN)
                            .header("X-Forwarded-For", "10.1.0.1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(BODY))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus()).isNotEqualTo(429)
                    );
        }
    }

    @Test
    void login_alSuperarLimite_retorna429() throws Exception {
        String ip = "10.1.0.2";

        // Primeras 5 peticiones → pasan el filtro
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post(LOGIN)
                    .header("X-Forwarded-For", ip)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(BODY));
        }

        // La 6ª petición → bloqueada por el filtro con 429 y código de error 28
        mockMvc.perform(post(LOGIN)
                        .header("X-Forwarded-For", ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().is(429))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(28));
    }

    // ─────────────────────────────────────────────────────────────
    // /v1/auth/register — límite 3/min
    // ─────────────────────────────────────────────────────────────

    @Test
    void register_alSuperarLimite_retorna429() throws Exception {
        String ip = "10.1.0.3";

        // Primeras 3 peticiones → pasan
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post(REGISTER)
                    .header("X-Forwarded-For", ip)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(BODY));
        }

        // La 4ª petición → 429
        mockMvc.perform(post(REGISTER)
                        .header("X-Forwarded-For", ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().is(429))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(28));
    }

    // ─────────────────────────────────────────────────────────────
    // Aislamiento por IP
    // ─────────────────────────────────────────────────────────────

    @Test
    void login_ipsDistintas_tienenBucketsIndependientes() throws Exception {
        // IP-A agota su cuota
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post(LOGIN)
                    .header("X-Forwarded-For", "10.1.0.4")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(BODY));
        }

        // IP-B aún no ha usado nada → debe pasar sin 429
        mockMvc.perform(post(LOGIN)
                        .header("X-Forwarded-For", "10.1.0.5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(result ->
                        assertThat(result.getResponse().getStatus()).isNotEqualTo(429)
                );
    }

    // ─────────────────────────────────────────────────────────────
    // Endpoints sin límite configurado no se bloquean
    // ─────────────────────────────────────────────────────────────

    @Test
    void refresh_sinLimiteConfigurado_nunca429() throws Exception {
        String body = "{\"refreshToken\":\"token-invalido\"}";

        // Más peticiones que cualquier límite → el filtro no debe bloquear /refresh
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post(REFRESH)
                            .header("X-Forwarded-For", "10.1.0.6")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus()).isNotEqualTo(429)
                    );
        }
    }
}
