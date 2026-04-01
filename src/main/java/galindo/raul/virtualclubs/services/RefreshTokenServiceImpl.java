package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.Dispositive;
import galindo.raul.virtualclubs.models.entities.DeviceEntity;
import galindo.raul.virtualclubs.models.entities.RefreshTokenEntity;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.repositories.DeviceRepository;
import galindo.raul.virtualclubs.repositories.RefreshTokenRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service class for managing refresh tokens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;
  private final DeviceRepository deviceRepository;

  /**
   * Saves a new refresh token for a specific device, replacing any existing token for that device.
   * If the device does not exist yet, it is created automatically.
   *
   * @param user        The user entity associated with the token.
   * @param hashedToken The hashed refresh token string.
   * @param dispositive Information about the device requesting the token.
   */
  @Transactional
  public void saveOrUpdate(UserEntity user, String hashedToken, Dispositive dispositive) {
    DeviceEntity device = deviceRepository.findByDeviceIdAndUser(dispositive.deviceId(), user)
        .orElseGet(() -> deviceRepository.save(DeviceEntity.builder()
            .deviceId(dispositive.deviceId())
            .deviceName(dispositive.deviceName())
            .deviceType(dispositive.deviceType())
            .ipAddress(dispositive.ipAddress())
            .user(user)
            .build()));

    refreshTokenRepository.deleteByUserAndDevice_DeviceId(user, dispositive.deviceId());
    refreshTokenRepository.flush();
    refreshTokenRepository.saveAndFlush(RefreshTokenEntity.builder()
        .user(user)
        .token(hashedToken)
        .device(device)
        .build());
  }

  @Transactional
  public void removeTokenFromDevice(UserEntity user, Dispositive dispositive) {
    refreshTokenRepository.findByUserAndDevice_DeviceIdAndRevokedFalse(user, dispositive.deviceId())
        .forEach(t -> t.setRevoked(true));
  }

  /**
   * Revoca todos los refresh tokens activos de un usuario. Se usa tras un reset de contraseña
   * para cerrar todas las sesiones abiertas.
   *
   * @param user el usuario cuyas sesiones se revocan
   */
  @Transactional
  public void revokeAllByUser(UserEntity user) {
    refreshTokenRepository.findByUserAndRevokedFalse(user)
        .forEach(t -> t.setRevoked(true));
  }

  /**
   * Elimina un refresh token específico si existe y no está revocado.
   *
   * @param user        usuario asociado al token
   * @param hashedToken hash del refresh token a eliminar
   * @return {@code true} si el token existía y fue eliminado; {@code false} en caso contrario
   */
  @Transactional
  public boolean removeToken(UserEntity user, String hashedToken) {
    return refreshTokenRepository.findByTokenAndUserAndRevokedFalse(hashedToken, user)
        .map(token -> {
          refreshTokenRepository.delete(token);
          return true;
        })
        .orElse(false);
  }
}
