package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.Dispositive;
import galindo.raul.virtualclubs.models.entities.DeviceEntity;
import galindo.raul.virtualclubs.models.entities.RefreshTokenEntity;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.repositories.DeviceRepository;
import galindo.raul.virtualclubs.repositories.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service class for managing refresh tokens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl {
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
   * Removes a specific refresh token for a user if it exists and is not revoked.
   *
   * @param user        The user entity associated with the token.
   * @param hashedToken The hashed refresh token string to be removed.
   * @return true if the token was found and deleted, false otherwise.
   */
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
