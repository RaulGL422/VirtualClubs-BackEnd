package galindo.raul.virtualclubs.config.security.utils;

import galindo.raul.virtualclubs.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

/**
 * Utility class for handling JSON Web Tokens (JWT) including generation, parsing, and validation.
 */
@Component
@RequiredArgsConstructor
public class JwtUtils {

  private final JwtProperties jwtProperties;

  /**
   * Generates a short-lived access token for the given username.
   *
   * @param username the subject of the token
   * @return a signed JWT access token
   */
  public String generateAccessToken(String username) {
    return generateToken(username, "access", jwtProperties.expiration());
  }

  /**
   * Generates a long-lived refresh token for the given username.
   *
   * @param username the subject of the token
   * @return a signed JWT refresh token
   */
  public String generateRefreshToken(String username) {
    return generateToken(username, "refresh", jwtProperties.refreshExpiration());
  }

  /**
   * Internal helper to build a JWT.
   * @param username the subject
   * @param type the custom claim "type" (access or refresh)
   * @param millisToExpire duration in milliseconds
   */
  private String generateToken(String username, String type, Long millisToExpire) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(millisToExpire)))
        .claim("type", type)
        .signWith(getSignatureKey())
        .compact();
  }

  /**
   * Validates if the token is structurally sound and matches the expected type.
   *
   * @param token the JWT string
   * @param type the expected type (e.g., "access")
   * @return true if valid, false otherwise
   */
  public boolean isTokenValid(String token, String type) {
    try {
      return getTypeFromToken(token).equals(type);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Extracts the username (subject) from the token.
   *
   * @param token the JWT string
   * @return the username
   */
  public String getUsernameFromToken(String token) {
    return getClaim(token, Claims::getSubject);
  }

  /**
   * Extracts the "type" claim from the token.
   *
   * @param token the JWT string
   * @return the token type
   */
  public String getTypeFromToken(String token) {
    return getClaim(token, (claims) -> claims.get("type", String.class));
  }

  /**
   * Generic method to extract a specific claim from the token.
   *
   * @param token the JWT string
   * @param claimsResolver a function to extract the desired claim
   * @param <T> the type of the claim
   * @return the extracted claim
   */
  private <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
    Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Parses the token and returns all claims.
   * @param token the JWT string
   * @return the Claims object
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSignatureKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  /**
   * Decodes the secret and generates the HMAC signing key.
   * @return the cryptographic SecretKey
   */
  private SecretKey getSignatureKey() {
    byte[] apiKeySecretBytes = Decoders.BASE64.decode(jwtProperties.secret());
    return Keys.hmacShaKeyFor(apiKeySecretBytes);
  }
}
