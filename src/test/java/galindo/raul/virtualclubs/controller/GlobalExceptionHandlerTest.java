package galindo.raul.virtualclubs.controller;

import galindo.raul.virtualclubs.models.exceptions.*;
import io.jsonwebtoken.JwtException;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests del GlobalExceptionHandler via un controlador stub.
 *
 * Cada test fuerza una excepción concreta y verifica que el handler devuelve
 * el HTTP status correcto y el código numérico de ErrorType esperado por el cliente móvil.
 *
 * No usa base de datos ni servicios reales — solo la capa MVC con seguridad abierta.
 */
@WebMvcTest(controllers = GlobalExceptionHandlerTest.StubController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    private static final String BASE = "/stub";

    @Autowired private MockMvc mockMvc;

    // ─────────────────────────────────────────────────────────────
    // MethodArgumentNotValidException (validación @Valid)
    // ─────────────────────────────────────────────────────────────

    @Test
    void campoBlanco_retorna400ConCodigo7() throws Exception {
        mockMvc.perform(post(BASE + "/valid")
                        .contentType("application/json")
                        .content("{\"value\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(7)); // FIELD_BLANK
    }

    // ─────────────────────────────────────────────────────────────
    // HttpMessageNotReadableException (JSON malformado)
    // ─────────────────────────────────────────────────────────────

    @Test
    void jsonMalformado_retorna400ConCodigo7() throws Exception {
        mockMvc.perform(post(BASE + "/valid")
                        .contentType("application/json")
                        .content("{esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(7)); // FIELD_BLANK
    }

    // ─────────────────────────────────────────────────────────────
    // Recursos — 404
    // ─────────────────────────────────────────────────────────────

    @Test
    void usernameNotFoundException_retorna404ConCodigo5() throws Exception {
        mockMvc.perform(get(BASE + "/username-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(5)); // USER_NOT_FOUND
    }

    @Test
    void emailNotFoundException_retorna404ConCodigo5() throws Exception {
        mockMvc.perform(get(BASE + "/email-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(5)); // USER_NOT_FOUND
    }

    // ─────────────────────────────────────────────────────────────
    // Conflicto — 409
    // ─────────────────────────────────────────────────────────────

    @Test
    void userAlreadyExistException_retorna409ConCodigo6() throws Exception {
        mockMvc.perform(get(BASE + "/user-already-exists"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(6)); // EMAIL_ALREADY_EXISTS
    }

    // ─────────────────────────────────────────────────────────────
    // Autenticación — 401
    // ─────────────────────────────────────────────────────────────

    @Test
    void refreshTokenException_retorna401ConCodigo3() throws Exception {
        mockMvc.perform(get(BASE + "/refresh-token-invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(3)); // INVALID_REFRESH_TOKEN
    }

    @Test
    void jwtException_retorna401ConCodigo3() throws Exception {
        mockMvc.perform(get(BASE + "/jwt-exception"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(3)); // INVALID_REFRESH_TOKEN
    }

    // ─────────────────────────────────────────────────────────────
    // Forbidden — 403
    // ─────────────────────────────────────────────────────────────

    @Test
    void emailNotVerifiedException_retorna403ConCodigo11() throws Exception {
        mockMvc.perform(get(BASE + "/email-not-verified"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(11)); // EMAIL_NOT_VERIFIED
    }

    @Test
    void invalidTokenException_retorna403ConCodigo4() throws Exception {
        mockMvc.perform(get(BASE + "/invalid-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(4)); // INVALID_TOKEN
    }

    // ─────────────────────────────────────────────────────────────
    // Bad Request — 400
    // ─────────────────────────────────────────────────────────────

    @Test
    void noLocalProviderException_retorna400ConCodigo12() throws Exception {
        mockMvc.perform(get(BASE + "/no-local-provider"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(12)); // NO_LOCAL_PROVIDER
    }

    // ─────────────────────────────────────────────────────────────
    // Internal Server Error — 500
    // ─────────────────────────────────────────────────────────────

    @Test
    void internalErrorException_retorna500ConCodigo1() throws Exception {
        mockMvc.perform(get(BASE + "/internal-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(1)); // INTERNAL_ERROR
    }

    @Test
    void excepcionGenerica_retorna500ConCodigo1() throws Exception {
        mockMvc.perform(get(BASE + "/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(1)); // INTERNAL_ERROR
    }

    // ─────────────────────────────────────────────────────────────
    // Stub controller — dispara cada excepción bajo demanda
    // ─────────────────────────────────────────────────────────────

    record StubBody(@NotBlank(message = "FIELD_BLANK") String value) {}

    @RestController
    @RequestMapping("/stub")
    static class StubController {

        @PostMapping("/valid")
        ResponseEntity<Void> bodyValido(@RequestBody @jakarta.validation.Valid StubBody body) {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/username-not-found")
        void usernameNotFound() { throw new UsernameNotFoundException("test"); }

        @GetMapping("/email-not-found")
        void emailNotFound() { throw new EmailNotFoundException("email@test.com"); }

        @GetMapping("/user-already-exists")
        void userAlreadyExists() { throw new UserAlreadyExistException("email@test.com"); }

        @GetMapping("/refresh-token-invalid")
        void refreshToken() { throw new RefreshTokenException(); }

        @GetMapping("/no-local-provider")
        void noLocalProvider() { throw new NoLocalProviderException("email@test.com"); }

        @GetMapping("/email-not-verified")
        void emailNotVerified() { throw new EmailNotVerifiedException("email@test.com"); }

        @GetMapping("/invalid-token")
        void invalidToken() { throw new InvalidTokenException(); }

        @GetMapping("/jwt-exception")
        void jwtException() { throw new JwtException("malformed jwt"); }

        @GetMapping("/internal-error")
        void internalError() { throw new InternalErrorException("server broke"); }

        @GetMapping("/generic-error")
        void genericError() { throw new RuntimeException("unexpected"); }
    }

    // ─────────────────────────────────────────────────────────────
    // Seguridad mínima: permite todos los requests al stub
    // ─────────────────────────────────────────────────────────────

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {

        // StubController es una clase interna del test y queda fuera del component scan
        // de la aplicación → hay que registrarla explícitamente para que
        // RequestMappingHandlerMapping la detecte.
        @Bean
        StubController stubController() {
            return new StubController();
        }

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }
}
