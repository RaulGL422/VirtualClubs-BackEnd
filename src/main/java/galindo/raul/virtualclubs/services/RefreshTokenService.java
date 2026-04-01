package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.Dispositive;
import galindo.raul.virtualclubs.models.entities.UserEntity;

/**
 * Contrato de negocio para la gestión de refresh tokens.
 */
public interface RefreshTokenService {

  /**
   * Guarda o reemplaza el refresh token de un dispositivo concreto.
   * Si el dispositivo no existía, lo crea automáticamente.
   *
   * @param user        usuario propietario del token
   * @param hashedToken hash SHA-256 Base64 del refresh token
   * @param dispositive información del dispositivo
   */
  void saveOrUpdate(UserEntity user, String hashedToken, Dispositive dispositive);

  /**
   * Revoca el refresh token activo del dispositivo actual (logout).
   *
   * @param user        usuario propietario del token
   * @param dispositive dispositivo desde el que se hace logout
   */
  void removeTokenFromDevice(UserEntity user, Dispositive dispositive);

  /**
   * Revoca todos los refresh tokens activos del usuario.
   * Se usa tras un reset de contraseña para cerrar todas las sesiones.
   *
   * @param user usuario cuyas sesiones se revocan
   */
  void revokeAllByUser(UserEntity user);

  /**
   * Elimina un refresh token específico si existe y no está revocado.
   *
   * @param user        usuario asociado al token
   * @param hashedToken hash del refresh token a eliminar
   * @return {@code true} si el token existía y fue eliminado
   */
  boolean removeToken(UserEntity user, String hashedToken);
}