package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.Dispositive;
import galindo.raul.virtualclubs.models.entities.RefreshTokenEntity;
import galindo.raul.virtualclubs.models.entities.UserEntity;
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
  
  @Transactional
  /**
   * Saves a new refresh token or updates an existing one for a specific device.
   *
   * @param user        The user entity associated with the token.
   * @param hashedToken The hashed refresh token string.
   * @param dispositive Information about the device requesting the token.
   */
  public void saveOrUpdate(UserEntity user, String hashedToken, Dispositive dispositive) {
    RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
        .user(user)
        .token(hashedToken)
        .deviceId(dispositive.deviceId())
        .deviceName(dispositive.deviceName())
        .deviceType(dispositive.deviceType())
        .ipAddress(dispositive.ipAddress())
        .build();
    
    refreshTokenRepository.findByUserAndRevokedFalse(user).stream()
        .filter(t -> t.getDeviceId().equals(dispositive.deviceId()) && !t.isRevoked())
        .forEach(t -> t.setRevoked(true));
    
    refreshTokenRepository.saveAndFlush(refreshTokenEntity);
  }
  
  @Transactional
  public void removeTokenFromDevice(UserEntity user, Dispositive dispositive) {
    refreshTokenRepository.findByUserAndDeviceIdAndRevokedFalse(user, dispositive.deviceId()).stream()
        .forEach(t -> t.setRevoked(true));
  }
  
  /**
   * Removes a specific refresh token for a user if it exists and is not revoked.
   *
   * @param user        The user entity associated with the token.
   * @param hashedToken The hashed refresh token string to be removed.
   * @return true if the token was found and deleted, false otherwise.
   */
  @Transactional
  public boolean removeToken(UserEntity user, String hashedToken) {
    
    if (refreshTokenRepository.existsByTokenAndUserAndRevokedFalse(hashedToken, user)) {
      refreshTokenRepository.deleteByUser(user);
      return true;
    }
    
    return false;
  }
}
