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
public class UserTokenServiceImpl implements UserTokenService {

  private final UserTokenRepository tokenRepository;
  private final TokenProperties tokenProperties;

  /**
   * Crea un token de un solo uso para el usuario y tipo indicados.
   * Si ya existía un token activo del mismo tipo para ese usuario, lo invalida primero.
   *
   * @param user usuario al que se le asocia el token
   * @param type tipo de token ({@link TokenType#EMAIL_VERIFICATION} o {@link TokenType#PASSWORD_RESET})
   * @return el token en texto plano — se debe enviar al usuario y nunca almacenar tal cual
   */
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

  /**
   * Valida el token en texto plano y, si es correcto, no expirado y no consumido,
   * lo marca como consumido y devuelve el usuario asociado.
   *
   * @param token token en texto plano recibido del usuario
   * @param type  tipo esperado del token
   * @return {@link Optional} con el usuario si el token es válido; vacío en caso contrario
   */
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

  /**
   * Invalida (elimina) todos los tokens del usuario del tipo indicado.
   * Útil para limpiar tokens pendientes antes de emitir uno nuevo o tras completar la acción.
   *
   * @param user usuario cuyos tokens se eliminan
   * @param type tipo de tokens a eliminar
   */
  @Transactional
  public void invalidateTokens(UserEntity user, TokenType type) {
    tokenRepository.deleteAllByUserAndType(user, type);
  }
}
