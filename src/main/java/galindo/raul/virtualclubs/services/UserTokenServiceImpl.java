package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.config.TokenProperties;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.entities.UserTokenEntity;
import galindo.raul.virtualclubs.models.enums.TokenType;
import galindo.raul.virtualclubs.repositories.UserTokenRepository;
import galindo.raul.virtualclubs.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTokenServiceImpl {

  private final UserTokenRepository tokenRepository;
  private final TokenProperties tokenProperties;

  @Transactional
  public String createTokenFor(UserEntity user, TokenType type) {
    tokenRepository.deleteAllByUserAndType(user, type);

    String token = TokenUtils.generateTokenString(32);
    String tokenHash = TokenUtils.sha256Hex(token);

    tokenRepository.save(UserTokenEntity.builder()
        .user(user)
        .tokenHash(tokenHash)
        .type(type)
        .expiresAt(Instant.now().plus(tokenProperties.getExpiryMinutes(type), ChronoUnit.MINUTES))
        .build());

    log.info("Token {} generado para '{}'", type, user.getEmail());
    return token;
  }

  @Transactional
  public Optional<UserEntity> validateAndConsume(String token, TokenType type) {
    String hash = TokenUtils.sha256Hex(token);
    return tokenRepository.findByTokenHashAndType(hash, type)
        .filter(t -> !t.isConsumed())
        .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
        .map(t -> {
          t.setConsumed(true);
          tokenRepository.save(t);
          log.info("Token {} consumido para '{}'", type, t.getUser().getEmail());
          return t.getUser();
        });
  }

  @Transactional
  public void invalidateTokens(UserEntity user, TokenType type) {
    tokenRepository.deleteAllByUserAndType(user, type);
  }
}
