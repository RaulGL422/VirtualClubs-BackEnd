package galindo.raul.virtualclubs.security;

import galindo.raul.virtualclubs.config.JwtProperties;
import galindo.raul.virtualclubs.config.security.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para JwtUtils.
 * No levanta contexto de Spring — instancia la clase directamente
 * pasando un JwtProperties de prueba al constructor.
 *
 * El secreto debe ser un string Base64 válido que decodifique a ≥ 32 bytes (HS256).
 * Decodifica a "test-secret-key-for-tests-only!!" (32 bytes).
 */
class JwtServiceTest {

    // Base64 de "test-secret-key-for-tests-only!!" (32 bytes — mínimo para HS256)
    private static final String SECRET_BASE64 = "dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0cy1vbmx5ISE=";
    private static final long   ACCESS_EXP    = 3_600_000L;    // 1 hora
    private static final long   REFRESH_EXP   = 604_800_000L;  // 7 días

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(new JwtProperties(SECRET_BASE64, ACCESS_EXP, REFRESH_EXP));
    }

    // ─────────────────────────────────────────────────────────────
    // Generación de tokens
    // ─────────────────────────────────────────────────────────────

    @Test
    void generateAccessToken_retornaTokenNoVacio() {
        String token = jwtUtils.generateAccessToken("user@test.com");
        assertThat(token).isNotBlank();
    }

    @Test
    void generateRefreshToken_retornaTokenNoVacio() {
        String token = jwtUtils.generateRefreshToken("user@test.com");
        assertThat(token).isNotBlank();
    }

    // ─────────────────────────────────────────────────────────────
    // Validación del tipo de token
    // ─────────────────────────────────────────────────────────────

    @Test
    void accessToken_esValidoComoAccess() {
        String token = jwtUtils.generateAccessToken("user@test.com");
        assertThat(jwtUtils.isTokenValid(token, "access")).isTrue();
    }

    @Test
    void refreshToken_esValidoComoRefresh() {
        String token = jwtUtils.generateRefreshToken("user@test.com");
        assertThat(jwtUtils.isTokenValid(token, "refresh")).isTrue();
    }

    @Test
    void accessToken_noEsValidoComoRefresh() {
        String token = jwtUtils.generateAccessToken("user@test.com");
        assertThat(jwtUtils.isTokenValid(token, "refresh")).isFalse();
    }

    @Test
    void refreshToken_noEsValidoComoAccess() {
        String token = jwtUtils.generateRefreshToken("user@test.com");
        assertThat(jwtUtils.isTokenValid(token, "access")).isFalse();
    }

    // ─────────────────────────────────────────────────────────────
    // Extracción del username
    // ─────────────────────────────────────────────────────────────

    @Test
    void getUsernameFromToken_retornaEmailCorrecto() {
        String token = jwtUtils.generateAccessToken("user@test.com");
        assertThat(jwtUtils.getUsernameFromToken(token)).isEqualTo("user@test.com");
    }

    // ─────────────────────────────────────────────────────────────
    // Tokens expirados y manipulados (isTokenValid devuelve false sin lanzar excepción)
    // ─────────────────────────────────────────────────────────────

    @Test
    void tokenExpirado_noEsValido() {
        // Con expiración 0 ms el token caduca en el instante de creación
        JwtUtils shortLived = new JwtUtils(new JwtProperties(SECRET_BASE64, 0L, 0L));

        String token = shortLived.generateAccessToken("user@test.com");
        assertThat(shortLived.isTokenValid(token, "access")).isFalse();
    }

    @Test
    void tokenTamperado_noEsValido() {
        String token = jwtUtils.generateAccessToken("user@test.com");
        assertThat(jwtUtils.isTokenValid(token + "tampered", "access")).isFalse();
    }

    @Test
    void tokenAleatorio_noEsValido() {
        assertThat(jwtUtils.isTokenValid("esto.no.es.un.jwt", "access")).isFalse();
    }
}
