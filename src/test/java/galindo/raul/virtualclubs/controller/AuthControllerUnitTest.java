package galindo.raul.virtualclubs.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import galindo.raul.virtualclubs.config.security.filters.JwtAuthFilter;
import galindo.raul.virtualclubs.config.security.utils.JwtUtils;
import galindo.raul.virtualclubs.models.Tokens;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.exceptions.InvalidTokenException;
import galindo.raul.virtualclubs.models.exceptions.UserAlreadyExistException;
import galindo.raul.virtualclubs.services.GoogleAuthService;
import galindo.raul.virtualclubs.services.RefreshTokenService;
import galindo.raul.virtualclubs.services.TokensService;
import galindo.raul.virtualclubs.services.UserEntityServiceImpl;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de capa web para AuthController usando @WebMvcTest.
 *
 * Carga solo los beans MVC (controller, filtros de seguridad, GlobalExceptionHandler)
 * y mockea toda la capa de servicio. Es mucho más rápido que @SpringBootTest porque
 * no levanta JPA ni BD.
 *
 * Qué verifica:
 * - Paths y métodos HTTP correctos
 * - Status codes (201, 400, 403, 409)
 * - Estructura de ApiResponse<T> y serialización JSON
 * - Bean Validation (@Valid, @StrongPassword, @Email, @Size)
 * - Cadena de filtros de seguridad (JwtAuthFilter)
 *
 * Para tests E2E con BD real, ver AuthControllerIntegrationTest.
 */
@WebMvcTest(AuthController.class)
@ActiveProfiles({"dev", "test"})
class AuthControllerUnitTest {

    private static final String BASE     = "/v1/auth";
    private static final String EMAIL    = "unit@test.com";
    private static final String PASSWORD = "PAss12"; // letras + dígitos, 6 chars — cumple @StrongPassword

    @Autowired private MockMvc mockMvc;

    @MockitoBean private UserEntityServiceImpl userService;
    @MockitoBean private JwtUtils jwtUtils;
    @MockitoBean private TokensService tokensService;
    @MockitoBean private RefreshTokenService refreshTokenService;
    @MockitoBean private galindo.raul.virtualclubs.services.NotificationService notificationService;
    @MockitoBean private galindo.raul.virtualclubs.services.UserTokenService userTokenService;
    @MockitoBean private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @MockitoBean private GoogleAuthService googleAuthService;

    // ─────────────────────────────────────────────────────────────
    // POST /v1/auth/register
    // ─────────────────────────────────────────────────────────────

    @Test
    void register_bodyValido_retorna201ConEstructuraCorrecta() throws Exception {
        UserEntity user = UserEntity.builder().id(1L).email(EMAIL).build();
        when(userService.registerUser(eq(EMAIL), eq(PASSWORD))).thenReturn(user);
        when(tokensService.getNewTokens(eq(user), any())).thenReturn(new Tokens("acc-token", "ref-token"));

        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("acc-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("ref-token"))
                .andExpect(jsonPath("$.data.email").value(EMAIL));
    }

    @Test
    void register_emailInvalido_retorna400() throws Exception {
        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("no-es-un-email", PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_passwordDebil_retorna400() throws Exception {
        // "abc" → 0 mayúsculas, 0 dígitos → falla @StrongPassword
        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, "abc")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_passwordCorto_retorna400() throws Exception {
        // "PAss1" → 5 chars → falla @Size(min=6)
        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, "PAss1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_usuarioDuplicado_retorna409() throws Exception {
        when(userService.registerUser(any(), any())).thenThrow(new UserAlreadyExistException(EMAIL));

        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─────────────────────────────────────────────────────────────
    // POST /v1/auth/refresh
    // ─────────────────────────────────────────────────────────────

    @Test
    void refresh_tokenVacio_retorna400() throws Exception {
        mockMvc.perform(post(BASE + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void refresh_tokenValido_retorna200ConNuevosTokens() throws Exception {
        UserEntity user = UserEntity.builder().id(1L).email(EMAIL).build();
        when(jwtUtils.getUsernameFromToken("old-token")).thenReturn(EMAIL);
        when(userService.getUserFromEmail(EMAIL)).thenReturn(user);
        when(tokensService.refreshTokens(eq(user), eq("old-token"), any()))
                .thenReturn(new Tokens("new-acc", "new-ref"));

        mockMvc.perform(post(BASE + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"old-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-acc"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-ref"));
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE /v1/auth/logout
    // ─────────────────────────────────────────────────────────────

    @Test
    void logout_sinAutenticacion_retorna403() throws Exception {
        mockMvc.perform(delete(BASE + "/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    void logout_conBearerTokenValido_retorna200() throws Exception {
        // Configurar JwtAuthFilter para que acepte el token de prueba
        UserDetails userDetails = User.withUsername(EMAIL).password("").roles("USER").build();
        when(jwtUtils.isTokenValid("test-access-token", "access")).thenReturn(true);
        when(jwtUtils.getUsernameFromToken("test-access-token")).thenReturn(EMAIL);
        when(userService.loadUserByUsername(EMAIL)).thenReturn(userDetails);

        // Configurar lo que necesita el propio método logout()
        UserEntity user = UserEntity.builder().id(1L).email(EMAIL).build();
        when(userService.getUserFromEmail(EMAIL)).thenReturn(user);

        mockMvc.perform(delete(BASE + "/logout")
                        .header("Authorization", "Bearer test-access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ─────────────────────────────────────────────────────────────
    // POST /v1/auth/google
    // ─────────────────────────────────────────────────────────────

    @Test
    void googleLogin_idTokenValido_retorna200ConTokens() throws Exception {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail(EMAIL);
        payload.setSubject("google-sub-123");
        payload.set("name", "Test User");

        UserEntity user = UserEntity.builder().id(1L).email(EMAIL).build();
        when(googleAuthService.verifyToken("valid-google-token")).thenReturn(payload);
        when(userService.registerOrLoadUserWithGoogle(eq(EMAIL), eq("Test User"), eq("google-sub-123")))
                .thenReturn(user);
        when(tokensService.getNewTokens(eq(user), any())).thenReturn(new Tokens("acc-token", "ref-token"));

        mockMvc.perform(post(BASE + "/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\": \"valid-google-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("acc-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("ref-token"));
    }

    @Test
    void googleLogin_idTokenBlanco_retorna400() throws Exception {
        mockMvc.perform(post(BASE + "/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void googleLogin_tokenRechazadoPorGoogle_retorna403ConErrorType4() throws Exception {
        when(googleAuthService.verifyToken("invalid-token")).thenThrow(new InvalidTokenException());

        mockMvc.perform(post(BASE + "/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\": \"invalid-token\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(4)); // ErrorType.INVALID_TOKEN = 4
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private String body(String email, String password) {
        return "{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}";
    }

    /**
     * Configuración de seguridad mínima para tests de capa web.
     *
     * @WebMvcTest no carga automáticamente SecurityConfig (es un @Configuration de tipo no-web),
     * así que aplicaría la política por defecto de Spring Security (CSRF activo + todo requiere auth).
     * Esta configuración interna replica las reglas de autorización de SecurityConfig y añade
     * JwtAuthFilter para que el test de logout con Bearer token funcione correctamente.
     */
    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http,
                                                    JwtUtils jwtUtils,
                                                    UserEntityServiceImpl userEntityService,
                                                    ObjectMapper objectMapper) throws Exception {
            JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtUtils, userEntityService, objectMapper);

            http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/v1/auth/login", "/v1/auth/register", "/v1/auth/refresh", "/v1/auth/google").permitAll()
                    .anyRequest().authenticated()
                )
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
        }
    }
}
