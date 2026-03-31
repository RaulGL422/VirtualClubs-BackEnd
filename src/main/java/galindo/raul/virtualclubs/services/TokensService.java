package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.config.security.utils.JwtUtils;
import galindo.raul.virtualclubs.models.Dispositive;
import galindo.raul.virtualclubs.models.Tokens;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.exceptions.RefreshTokenException;
import galindo.raul.virtualclubs.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service responsible for managing JWT access and refresh tokens.
 */
@Service
@RequiredArgsConstructor
public class TokensService {
  private final JwtUtils jwtUtils;
  private final RefreshTokenService refreshTokenService;
  
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
    
    String hashedRefreshToken = TokenUtils.sha256Base64(refreshToken);
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
    String hashedToken = TokenUtils.sha256Base64(refreshToken);
    
    if (jwtUtils.isTokenValid(refreshToken, "refresh") && refreshTokenService.removeToken(user, hashedToken)) {
      return getNewTokens(user, dispositive);
    }
    
    throw new RefreshTokenException();
  }
  
}
