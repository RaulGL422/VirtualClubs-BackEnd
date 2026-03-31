package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.config.security.utils.JwtUtils;
import galindo.raul.virtualclubs.models.Dispositive;
import galindo.raul.virtualclubs.models.Tokens;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.exceptions.RefreshTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TokensService.
 * Sin contexto Spring — Mockito puro.
 */
@ExtendWith(MockitoExtension.class)
class TokensServiceTest {

    @Mock private JwtUtils jwtUtils;
    @Mock private RefreshTokenServiceImpl refreshTokenService;

    @InjectMocks private TokensService tokensService;

    private UserEntity user;
    private Dispositive dispositive;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder().id(1L).email("tokens@test.com").build();
        dispositive = new Dispositive("device-1", "Test Device", "ANDROID", "127.0.0.1");
    }

    // ─────────────────────────────────────────────────────────────
    // getNewTokens
    // ─────────────────────────────────────────────────────────────

    @Test
    void getNewTokens_retornaAccessYRefreshToken() {
        when(jwtUtils.generateAccessToken("tokens@test.com")).thenReturn("acc-token");
        when(jwtUtils.generateRefreshToken("tokens@test.com")).thenReturn("ref-token");

        Tokens tokens = tokensService.getNewTokens(user, dispositive);

        assertThat(tokens.accessToken()).isEqualTo("acc-token");
        assertThat(tokens.refreshToken()).isEqualTo("ref-token");
    }

    @Test
    void getNewTokens_guardaHashDelRefreshTokenEnBD() {
        when(jwtUtils.generateAccessToken(any())).thenReturn("acc-token");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("ref-token");

        tokensService.getNewTokens(user, dispositive);

        // El hash almacenado no debe ser el token en texto plano
        verify(refreshTokenService).saveOrUpdate(eq(user), argThat(hash ->
                hash != null && !hash.equals("ref-token")
        ), eq(dispositive));
    }

    @Test
    void getNewTokens_elMismoTokenSiempreProduceElMismoHash() {
        when(jwtUtils.generateAccessToken(any())).thenReturn("acc");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("same-token");

        tokensService.getNewTokens(user, dispositive);
        tokensService.getNewTokens(user, dispositive);

        // Mismo token → mismo hash (determinístico)
        verify(refreshTokenService, times(2)).saveOrUpdate(eq(user), argThat(hash ->
                hash != null && !hash.equals("same-token")
        ), eq(dispositive));
    }

    // ─────────────────────────────────────────────────────────────
    // refreshTokens
    // ─────────────────────────────────────────────────────────────

    @Test
    void refreshTokens_tokenValidoYEncontrado_retornaNuevosPar() {
        when(jwtUtils.isTokenValid("old-refresh", "refresh")).thenReturn(true);
        when(refreshTokenService.removeToken(eq(user), any())).thenReturn(true);
        when(jwtUtils.generateAccessToken(any())).thenReturn("new-acc");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("new-ref");

        Tokens result = tokensService.refreshTokens(user, "old-refresh", dispositive);

        assertThat(result.accessToken()).isEqualTo("new-acc");
        assertThat(result.refreshToken()).isEqualTo("new-ref");
    }

    @Test
    void refreshTokens_tokenJwtInvalido_lanzaRefreshTokenException() {
        when(jwtUtils.isTokenValid("bad-token", "refresh")).thenReturn(false);

        assertThatThrownBy(() -> tokensService.refreshTokens(user, "bad-token", dispositive))
                .isInstanceOf(RefreshTokenException.class);

        verify(refreshTokenService, never()).saveOrUpdate(any(), any(), any());
    }

    @Test
    void refreshTokens_tokenValidoPeroNoEnBD_lanzaRefreshTokenException() {
        when(jwtUtils.isTokenValid("valid-jwt", "refresh")).thenReturn(true);
        when(refreshTokenService.removeToken(eq(user), any())).thenReturn(false);

        assertThatThrownBy(() -> tokensService.refreshTokens(user, "valid-jwt", dispositive))
                .isInstanceOf(RefreshTokenException.class);

        verify(refreshTokenService, never()).saveOrUpdate(any(), any(), any());
    }

    @Test
    void refreshTokens_tokenValido_eliminaElTokenAntiguo() {
        when(jwtUtils.isTokenValid("old-refresh", "refresh")).thenReturn(true);
        when(refreshTokenService.removeToken(eq(user), any())).thenReturn(true);
        when(jwtUtils.generateAccessToken(any())).thenReturn("new-acc");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("new-ref");

        tokensService.refreshTokens(user, "old-refresh", dispositive);

        // Verifica que removeToken fue llamado con el hash del token viejo
        verify(refreshTokenService).removeToken(eq(user), argThat(hash ->
                hash != null && !hash.equals("old-refresh")
        ));
    }
}