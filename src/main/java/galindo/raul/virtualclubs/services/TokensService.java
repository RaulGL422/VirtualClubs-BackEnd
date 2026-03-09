package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.config.security.utils.JwtUtils;
import galindo.raul.virtualclubs.models.Dispositive;
import galindo.raul.virtualclubs.models.Tokens;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.exceptions.RefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Base64;

/**
 * Service responsible for managing JWT access and refresh tokens.
 */
@Service
@RequiredArgsConstructor
public class TokensService {
  private final JwtUtils jwtUtils;
  private final RefreshTokenServiceImpl refreshTokenService;
  
  /**
   * Generates a new pair of access and refresh tokens for a user and device.
   *
   * @param user        The user entity.
   * @param dispositive The device information.
   * @return A Tokens object containing the new access and refresh tokens.
   */
  public Tokens getNewTokens(UserEntity user, Dispositive dispositive) {
    String accessToken = jwtUtils.generateAccessToken(user.getEmail());
    String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());
    
    String hashedRefreshToken = _hashToken(refreshToken);
    refreshTokenService.saveOrUpdate(user, hashedRefreshToken, dispositive);
    
    return new Tokens(accessToken, refreshToken);
  }
  
  /**
   * Validates a refresh token and generates a new set of tokens if valid.
   *
   * @param user         The user entity.
   * @param refreshToken The current refresh token.
   * @param dispositive  The device information.
   * @return A new Tokens object.
   * @throws RefreshTokenException if the token is invalid or not found.
   */
  public Tokens refreshTokens(UserEntity user, String refreshToken, Dispositive dispositive) {
    String hashedToken = _hashToken(refreshToken);
    
    if (jwtUtils.isTokenValid(refreshToken, "refresh") && refreshTokenService.removeToken(user, hashedToken)) {
      return getNewTokens(user, dispositive);
    }
    
    throw new RefreshTokenException();
  }
  
  /**
   * Hashes a token using SHA-256 for secure storage.
   *
   * @param token The raw token string.
   * @return The Base64 encoded hash of the token.
   */
  public String _hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes());
      return Base64.getEncoder().encodeToString(hash);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
