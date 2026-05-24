package galindo.raul.virtualclubs.security;

import galindo.raul.virtualclubs.models.enums.ErrorType;
import galindo.raul.virtualclubs.services.EmailService;
import galindo.raul.virtualclubs.services.GoogleAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para verificar que SecurityConfig autoriza correctamente
 * cada endpoint: públicos accesibles sin token, protegidos bloqueados sin token,
 * y que un Bearer inválido devuelve 401 con el código correcto en TODOS los endpoints.
 *
 * Usa el contexto completo (H2 + JwtAuthFilter real) para validar la cadena de filtros
 * tal como funciona en producción.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
class SecurityConfigIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private GoogleAuthService googleAuthService;
    @MockitoBean private EmailService emailService;

    // ─────────────────────────────────────────────────────────────
    // Endpoints públicos — no deben devolver 403 sin token
    // ─────────────────────────────────────────────────────────────

    @Test
    void register_sinToken_esAccesible() throws Exception {
        // Sin body → 400 de validación, pero NO 403 de seguridad
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_sinToken_esAccesible() throws Exception {
        mockMvc.perform(post("/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestPasswordReset_sinToken_esAccesible() throws Exception {
        mockMvc.perform(post("/v1/auth/requestPasswordReset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_sinToken_esAccesible() throws Exception {
        mockMvc.perform(post("/v1/auth/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void googleAuth_sinToken_esAccesible() throws Exception {
        mockMvc.perform(post("/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────────────────────────────────────────────
    // Endpoint protegido — sin token devuelve 403
    // ─────────────────────────────────────────────────────────────

    @Test
    void logout_sinToken_retorna403() throws Exception {
        mockMvc.perform(delete("/v1/auth/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestVerify_sinToken_retorna403() throws Exception {
        mockMvc.perform(post("/v1/auth/requestVerify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────────────────────
    // Bearer inválido — JwtAuthFilter intercepta con 401 + código 4
    // Aplica a cualquier endpoint, incluso públicos
    // ─────────────────────────────────────────────────────────────

    @Test
    void logout_conBearerInvalido_retorna401ConCodigo4() throws Exception {
        mockMvc.perform(delete("/v1/auth/logout")
                        .header("Authorization", "Bearer not-a-real-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorType.INVALID_TOKEN.getCode())); // 4
    }

    @Test
    void endpointPublico_conBearerInvalido_retorna401() throws Exception {
        // JwtAuthFilter intercepta antes de que llegue al controller — incluso en rutas permitAll()
        mockMvc.perform(post("/v1/auth/register")
                        .header("Authorization", "Bearer not-a-real-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorType.INVALID_TOKEN.getCode())); // 4
    }

    // ─────────────────────────────────────────────────────────────
    // Endpoint inexistente — Spring MVC retorna 404, no 403
    // ─────────────────────────────────────────────────────────────

    @Test
    void endpointInexistente_sinToken_retorna403() throws Exception {
        // Spring Security evalúa la autorización antes de que Spring MVC busque el handler.
        // Sin token, anyRequest().authenticated() bloquea con 403 aunque la ruta no exista.
        mockMvc.perform(get("/v1/nonexistent"))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointInexistente_conBearerInvalido_retorna401() throws Exception {
        // El filtro intercepta antes de que Spring Security evalúe la ruta
        mockMvc.perform(get("/v1/nonexistent")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized());
    }
}
