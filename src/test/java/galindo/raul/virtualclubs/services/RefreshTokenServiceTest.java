package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.Dispositive;
import galindo.raul.virtualclubs.models.entities.RefreshTokenEntity;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.repositories.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RefreshTokenServiceImpl.
 * Usa Mockito para simular el repositorio — no necesita base de datos.
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl service;

    private UserEntity user;
    private Dispositive dispositive;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder().id(1L).email("user@test.com").build();
        dispositive = new Dispositive("device-id-123", "Test Device", "ANDROID", "127.0.0.1");
    }

    // ─────────────────────────────────────────────────────────────
    // saveOrUpdate
    // ─────────────────────────────────────────────────────────────

    @Test
    void saveOrUpdate_sinTokenPrevio_guardaNuevoToken() {
        service.saveOrUpdate(user, "hashed-token-nuevo", dispositive);

        verify(refreshTokenRepository).deleteByUserAndDeviceId(user, "device-id-123");
        verify(refreshTokenRepository).saveAndFlush(argThat(rt ->
                rt.getToken().equals("hashed-token-nuevo") &&
                rt.getDeviceId().equals("device-id-123")));
    }

    @Test
    void saveOrUpdate_conTokenExistenteDelMismoDispositivo_eliminaElViejoYGuardaElNuevo() {
        service.saveOrUpdate(user, "token-nuevo", dispositive);

        // El token del mismo dispositivo se elimina antes de insertar el nuevo
        verify(refreshTokenRepository).deleteByUserAndDeviceId(user, "device-id-123");
        verify(refreshTokenRepository).saveAndFlush(argThat(rt -> rt.getToken().equals("token-nuevo")));
    }

    @Test
    void saveOrUpdate_conTokenDeOtroDispositivo_noEliminaElOtroToken() {
        Dispositive otroDispositivo = new Dispositive("otro-device-id", "Otro Device", "IOS", "192.168.1.1");

        service.saveOrUpdate(user, "token-nuevo", dispositive);

        // Solo elimina tokens del dispositivo actual, no del otro
        verify(refreshTokenRepository).deleteByUserAndDeviceId(user, "device-id-123");
        verify(refreshTokenRepository, never()).deleteByUserAndDeviceId(user, "otro-device-id");
    }

    // ─────────────────────────────────────────────────────────────
    // removeTokenFromDevice
    // ─────────────────────────────────────────────────────────────

    @Test
    void removeTokenFromDevice_tokenExistente_loRevoca() {
        RefreshTokenEntity rt = RefreshTokenEntity.builder()
                .token("token-activo")
                .deviceId("device-id-123")
                .user(user)
                .build();

        when(refreshTokenRepository.findByUserAndDeviceIdAndRevokedFalse(user, "device-id-123"))
                .thenReturn(List.of(rt));

        service.removeTokenFromDevice(user, dispositive);

        assertThat(rt.isRevoked()).isTrue();
    }

    @Test
    void removeTokenFromDevice_sinToken_noHaceNada() {
        when(refreshTokenRepository.findByUserAndDeviceIdAndRevokedFalse(user, "device-id-123"))
                .thenReturn(Collections.emptyList());

        service.removeTokenFromDevice(user, dispositive);

        // Ninguna escritura en repositorio
        verify(refreshTokenRepository, never()).saveAndFlush(any());
        verify(refreshTokenRepository, never()).deleteByUser(any());
    }

    // ─────────────────────────────────────────────────────────────
    // removeToken
    // ─────────────────────────────────────────────────────────────

    @Test
    void removeToken_tokenValidoYActivo_eliminaTodosLosTokensYRetornaTrue() {
        when(refreshTokenRepository.existsByTokenAndUserAndRevokedFalse("token-hash", user))
                .thenReturn(true);

        boolean resultado = service.removeToken(user, "token-hash");

        assertThat(resultado).isTrue();
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void removeToken_tokenNoExisteORevocado_retornaFalse() {
        when(refreshTokenRepository.existsByTokenAndUserAndRevokedFalse("token-hash", user))
                .thenReturn(false);

        boolean resultado = service.removeToken(user, "token-hash");

        assertThat(resultado).isFalse();
        verify(refreshTokenRepository, never()).deleteByUser(any());
    }
}
