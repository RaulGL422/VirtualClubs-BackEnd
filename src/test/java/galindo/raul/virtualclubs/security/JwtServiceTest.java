package galindo.raul.virtualclubs.security;

import galindo.raul.virtualclubs.services.VirtualClubsUsersDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests unitarios para JwtService.
 * No levanta contexto de Spring — solo instancia el servicio directamente.
 */
class JwtServiceTest {

    private static final String SECRET = "test-jwt-secret-for-unit-and-integration-tests-only";
    private static final long ACCESS_EXP_MS  = 3_600_000L;  // 1 hora
    private static final long REFRESH_EXP_MS = 604_800_000L; // 7 días

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                SECRET,
                mock(VirtualClubsUsersDetailsService.class),
                ACCESS_EXP_MS,
                REFRESH_EXP_MS
        );
    }

    // ─────────────────────────────────────────────────────────────
    // Generación de tokens
    // ─────────────────────────────────────────────────────────────

    @Test
    void generateAccessToken_retornaTokenNoVacio() {
        String token = jwtService.generateAccessToken("user@test.com");
        assertThat(token).isNotBlank();
    }

    @Test
    void generateRefreshToken_retornaTokenNoVacio() {
        String token = jwtService.generateRefreshToken("user@test.com");
        assertThat(token).isNotBlank();
    }

    // ─────────────────────────────────────────────────────────────
    // Validación del tipo de token
    // ─────────────────────────────────────────────────────────────

    @Test
    void accessToken_esValidoComoAccess() {
        String token = jwtService.generateAccessToken("user@test.com");
        assertThat(jwtService.isValidAccessToken(token)).isTrue();
    }

    @Test
    void refreshToken_esValidoComoRefresh() {
        String token = jwtService.generateRefreshToken("user@test.com");
        assertThat(jwtService.isValidRefreshToken(token)).isTrue();
    }

    @Test
    void accessToken_noEsValidoComoRefresh() {
        String token = jwtService.generateAccessToken("user@test.com");
        assertThat(jwtService.isValidRefreshToken(token)).isFalse();
    }

    @Test
    void refreshToken_noEsValidoComoAccess() {
        String token = jwtService.generateRefreshToken("user@test.com");
        assertThat(jwtService.isValidAccessToken(token)).isFalse();
    }

    // ─────────────────────────────────────────────────────────────
    // Extracción del email
    // ─────────────────────────────────────────────────────────────

    @Test
    void extractEmail_retornaEmailCorrecto() {
        String token = jwtService.generateAccessToken("user@test.com");
        assertThat(jwtService.extractEmail(token)).isEqualTo("user@test.com");
    }

    @Test
    void extractEmail_tokenTamperado_retornaNull() {
        String token = jwtService.generateAccessToken("user@test.com");
        assertThat(jwtService.extractEmail(token + "x")).isNull();
    }

    // ─────────────────────────────────────────────────────────────
    // Tokens expirados y manipulados
    // ─────────────────────────────────────────────────────────────

    @Test
    void tokenExpirado_noEsValido() {
        // Con expiración 0 ms el token caduca en el instante de creación
        JwtService shortLived = new JwtService(
                SECRET,
                mock(VirtualClubsUsersDetailsService.class),
                0L,
                0L
        );
        String token = shortLived.generateAccessToken("user@test.com");
        assertThat(shortLived.isValidAccessToken(token)).isFalse();
    }

    @Test
    void tokenTamperado_noEsValido() {
        String token = jwtService.generateAccessToken("user@test.com");
        assertThat(jwtService.isValidAccessToken(token + "tampered")).isFalse();
    }

    @Test
    void tokenAleatorio_noEsValido() {
        assertThat(jwtService.isValidAccessToken("esto.no.es.un.jwt")).isFalse();
    }
}
