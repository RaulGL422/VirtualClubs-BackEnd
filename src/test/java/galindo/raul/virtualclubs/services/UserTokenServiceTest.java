package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.config.TokenProperties;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.entities.UserTokenEntity;
import galindo.raul.virtualclubs.models.enums.TokenType;
import galindo.raul.virtualclubs.repositories.UserTokenRepository;
import galindo.raul.virtualclubs.utils.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UserTokenServiceImpl.
 * Sin contexto Spring — Mockito puro.
 */
@ExtendWith(MockitoExtension.class)
class UserTokenServiceTest {

    @Mock private UserTokenRepository tokenRepository;
    @Mock private TokenProperties tokenProperties;

    @InjectMocks private UserTokenServiceImpl userTokenService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder().id(1L).email("test@test.com").build();
    }

    // ─────────────────────────────────────────────────────────────
    // createTokenFor
    // ─────────────────────────────────────────────────────────────

    @Test
    void createTokenFor_eliminaTokensPreviosYGuardaNuevo() {
        when(tokenProperties.getExpiryMinutes(TokenType.EMAIL_VERIFICATION)).thenReturn(1440);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String token = userTokenService.createTokenFor(user, TokenType.EMAIL_VERIFICATION);

        verify(tokenRepository).deleteAllByUserAndType(user, TokenType.EMAIL_VERIFICATION);
        ArgumentCaptor<UserTokenEntity> captor = ArgumentCaptor.forClass(UserTokenEntity.class);
        verify(tokenRepository).save(captor.capture());

        UserTokenEntity saved = captor.getValue();
        assertThat(saved.getTokenHash()).isEqualTo(TokenUtils.sha256Hex(token));
        assertThat(saved.getType()).isEqualTo(TokenType.EMAIL_VERIFICATION);
        assertThat(saved.isConsumed()).isFalse();
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void createTokenFor_tokenRetornadoNoEsElHash() {
        when(tokenProperties.getExpiryMinutes(TokenType.EMAIL_VERIFICATION)).thenReturn(1440);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String token = userTokenService.createTokenFor(user, TokenType.EMAIL_VERIFICATION);

        // El token devuelto es el plaintext, no el hash
        assertThat(token).isNotEqualTo(TokenUtils.sha256Hex(token));
        // 32 bytes en Base64URL sin padding = 43 chars
        assertThat(token).hasSize(43);
    }

    // ─────────────────────────────────────────────────────────────
    // validateAndConsume — casos válidos
    // ─────────────────────────────────────────────────────────────

    @Test
    void validateAndConsume_tokenValido_retornaUsuarioYMarcaConsumed() {
        String token = "plaintext-token";
        String hash = TokenUtils.sha256Hex(token);

        UserTokenEntity entity = UserTokenEntity.builder()
                .user(user)
                .tokenHash(hash)
                .type(TokenType.EMAIL_VERIFICATION)
                .expiresAt(Instant.now().plusSeconds(3600))
                .consumed(false)
                .build();

        when(tokenRepository.findByTokenHashAndType(hash, TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(entity));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<UserEntity> result = userTokenService.validateAndConsume(token, TokenType.EMAIL_VERIFICATION);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@test.com");
        assertThat(entity.isConsumed()).isTrue();
        verify(tokenRepository).save(entity);
    }

    // ─────────────────────────────────────────────────────────────
    // validateAndConsume — casos de fallo
    // ─────────────────────────────────────────────────────────────

    @Test
    void validateAndConsume_tokenNoEncontrado_retornaVacio() {
        when(tokenRepository.findByTokenHashAndType(any(), any())).thenReturn(Optional.empty());

        Optional<UserEntity> result = userTokenService.validateAndConsume("inexistente", TokenType.EMAIL_VERIFICATION);

        assertThat(result).isEmpty();
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void validateAndConsume_tokenYaConsumed_retornaVacio() {
        String token = "already-used";
        String hash = TokenUtils.sha256Hex(token);

        UserTokenEntity entity = UserTokenEntity.builder()
                .user(user)
                .tokenHash(hash)
                .type(TokenType.EMAIL_VERIFICATION)
                .expiresAt(Instant.now().plusSeconds(3600))
                .consumed(true)
                .build();

        when(tokenRepository.findByTokenHashAndType(hash, TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(entity));

        Optional<UserEntity> result = userTokenService.validateAndConsume(token, TokenType.EMAIL_VERIFICATION);

        assertThat(result).isEmpty();
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void validateAndConsume_tokenExpirado_retornaVacio() {
        String token = "expired-token";
        String hash = TokenUtils.sha256Hex(token);

        UserTokenEntity entity = UserTokenEntity.builder()
                .user(user)
                .tokenHash(hash)
                .type(TokenType.EMAIL_VERIFICATION)
                .expiresAt(Instant.now().minusSeconds(1))
                .consumed(false)
                .build();

        when(tokenRepository.findByTokenHashAndType(hash, TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(entity));

        Optional<UserEntity> result = userTokenService.validateAndConsume(token, TokenType.EMAIL_VERIFICATION);

        assertThat(result).isEmpty();
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void validateAndConsume_tipoIncorrecto_retornaVacio() {
        // El hash existe pero se pide con un tipo distinto → repository devuelve vacío
        String token = "valid-token";
        String hash = TokenUtils.sha256Hex(token);

        when(tokenRepository.findByTokenHashAndType(hash, TokenType.PASSWORD_RESET))
                .thenReturn(Optional.empty());

        Optional<UserEntity> result = userTokenService.validateAndConsume(token, TokenType.PASSWORD_RESET);

        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────
    // invalidateTokens
    // ─────────────────────────────────────────────────────────────

    @Test
    void invalidateTokens_delegaAlRepositorio() {
        userTokenService.invalidateTokens(user, TokenType.EMAIL_VERIFICATION);
        verify(tokenRepository).deleteAllByUserAndType(user, TokenType.EMAIL_VERIFICATION);
    }
}