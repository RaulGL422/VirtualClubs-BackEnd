package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.enums.TokenType;

import java.util.Optional;

/**
 * Contrato de negocio para la gestión de tokens de un solo uso
 * (verificación de email y reset de contraseña).
 */
public interface UserTokenService {

  /**
   * Crea un token de un solo uso para el usuario y tipo indicados.
   * Si ya existía un token activo del mismo tipo para ese usuario, lo invalida primero.
   *
   * @param user usuario al que se asocia el token
   * @param type tipo de token ({@link TokenType#EMAIL_VERIFICATION} o {@link TokenType#PASSWORD_RESET})
   * @return el token en texto plano — se debe enviar al usuario y nunca almacenar tal cual
   */
  String createTokenFor(UserEntity user, TokenType type);

  /**
   * Valida el token en texto plano y, si es correcto, no expirado y no consumido,
   * lo marca como consumido y devuelve el usuario asociado.
   *
   * @param token token en texto plano recibido del usuario
   * @param type  tipo esperado del token
   * @return {@link Optional} con el usuario si el token es válido; vacío en caso contrario
   */
  Optional<UserEntity> validateAndConsume(String token, TokenType type);

  /**
   * Invalida todos los tokens del usuario del tipo indicado.
   *
   * @param user usuario cuyos tokens se eliminan
   * @param type tipo de tokens a eliminar
   */
  void invalidateTokens(UserEntity user, TokenType type);
}