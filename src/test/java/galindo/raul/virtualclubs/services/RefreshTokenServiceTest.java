package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.Dispositive;
import galindo.raul.virtualclubs.models.entities.DeviceEntity;
import galindo.raul.virtualclubs.models.entities.RefreshTokenEntity;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.repositories.DeviceRepository;
import galindo.raul.virtualclubs.repositories.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private RefreshTokenServiceImpl service;

    private UserEntity user;
    private Dispositive dispositive;
    private DeviceEntity device;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder().id(1L).email("user@test.com").build();
        dispositive = new Dispositive("device-id-123", "Test Device", "ANDROID", "127.0.0.1");
        device = DeviceEntity.builder()
                .id(1L)
                .deviceId("device-id-123")
                .deviceName("Test Device")
                .deviceType("ANDROID")
                .ipAddress("127.0.0.1")
                .user(user)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // saveOrUpdate
    // ─────────────────────────────────────────────────────────────

    @Test
    void saveOrUpdate_dispositivoNuevo_creaDeviceYGuardaToken() {
        when(deviceRepository.findByDeviceIdAndUser("device-id-123", user))
                .thenReturn(Optional.empty());
        when(deviceRepository.save(any(DeviceEntity.class))).thenReturn(device);

        service.saveOrUpdate(user, "hashed-token-nuevo", dispositive);

        verify(deviceRepository).save(argThat(d -> d.getDeviceId().equals("device-id-123")));
        verify(refreshTokenRepository).deleteByUserAndDevice_DeviceId(user, "device-id-123");
        verify(refreshTokenRepository).saveAndFlush(argThat(rt ->
                rt.getToken().equals("hashed-token-nuevo") &&
                rt.getDevice().getDeviceId().equals("device-id-123")));
    }

    @Test
    void saveOrUpdate_dispositivoExistente_reutilizaDeviceYActualizaToken() {
        when(deviceRepository.findByDeviceIdAndUser("device-id-123", user))
                .thenReturn(Optional.of(device));

        service.saveOrUpdate(user, "token-nuevo", dispositive);

        // No crea un nuevo device
        verify(deviceRepository, never()).save(any(DeviceEntity.class));
        verify(refreshTokenRepository).deleteByUserAndDevice_DeviceId(user, "device-id-123");
        verify(refreshTokenRepository).saveAndFlush(argThat(rt -> rt.getToken().equals("token-nuevo")));
    }

    @Test
    void saveOrUpdate_conTokenDeOtroDispositivo_noEliminaElOtroToken() {
        when(deviceRepository.findByDeviceIdAndUser("device-id-123", user))
                .thenReturn(Optional.of(device));

        service.saveOrUpdate(user, "token-nuevo", dispositive);

        // Solo elimina tokens del dispositivo actual, no de otros
        verify(refreshTokenRepository).deleteByUserAndDevice_DeviceId(user, "device-id-123");
        verify(refreshTokenRepository, never()).deleteByUserAndDevice_DeviceId(user, "otro-device-id");
    }

    // ─────────────────────────────────────────────────────────────
    // removeTokenFromDevice
    // ─────────────────────────────────────────────────────────────

    @Test
    void removeTokenFromDevice_tokenExistente_loRevoca() {
        RefreshTokenEntity rt = RefreshTokenEntity.builder()
                .token("token-activo")
                .device(device)
                .user(user)
                .build();

        when(refreshTokenRepository.findByUserAndDevice_DeviceIdAndRevokedFalse(user, "device-id-123"))
                .thenReturn(List.of(rt));

        service.removeTokenFromDevice(user, dispositive);

        assertThat(rt.isRevoked()).isTrue();
    }

    @Test
    void removeTokenFromDevice_sinToken_noHaceNada() {
        when(refreshTokenRepository.findByUserAndDevice_DeviceIdAndRevokedFalse(user, "device-id-123"))
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
    void removeToken_tokenValidoYActivo_eliminaSoloEseTokenYRetornaTrue() {
        RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
                .token("token-hash")
                .user(user)
                .device(device)
                .build();
        when(refreshTokenRepository.findByTokenAndUserAndRevokedFalse("token-hash", user))
                .thenReturn(Optional.of(tokenEntity));

        boolean resultado = service.removeToken(user, "token-hash");

        assertThat(resultado).isTrue();
        verify(refreshTokenRepository).delete(tokenEntity);
        verify(refreshTokenRepository, never()).deleteByUser(any());
    }

    @Test
    void removeToken_tokenNoExisteORevocado_retornaFalse() {
        when(refreshTokenRepository.findByTokenAndUserAndRevokedFalse("token-hash", user))
                .thenReturn(Optional.empty());

        boolean resultado = service.removeToken(user, "token-hash");

        assertThat(resultado).isFalse();
        verify(refreshTokenRepository, never()).delete(any(RefreshTokenEntity.class));
    }
}
