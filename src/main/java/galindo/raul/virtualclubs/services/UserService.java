package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.entities.UserEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

/**
 * Contrato de negocio para la gestión de usuarios.
 * Extiende {@link UserDetailsService} para integrarse con Spring Security.
 */
public interface UserService extends UserDetailsService {

  /**
   * Registra un nuevo usuario con proveedor LOCAL.
   *
   * @param email    email del usuario
   * @param password contraseña en texto plano (será codificada)
   * @return el {@link UserEntity} persistido
   */
  UserEntity registerUser(String email, String password);

  /**
   * Procesa un login/registro con Google OAuth2.
   * Crea cuenta nueva, vincula con LOCAL existente verificado o devuelve la cuenta Google existente.
   *
   * @param email    email del usuario según Google
   * @param name     nombre del usuario según Google
   * @param googleId identificador único de Google
   * @return el {@link UserEntity} resultante
   */
  UserEntity registerOrLoadUserWithGoogle(String email, String name, String googleId);

  /**
   * Busca un usuario por email sin lanzar excepción si no existe.
   *
   * @param email email a buscar
   * @return {@link Optional} con el usuario si existe
   */
  Optional<UserEntity> findByEmailOptional(String email);

  /**
   * Devuelve el usuario por email o lanza excepción si no existe.
   *
   * @param email email a buscar
   * @return el {@link UserEntity} encontrado
   */
  UserEntity getUserFromEmail(String email);

  /**
   * Persiste cambios en un {@link UserEntity} existente.
   *
   * @param user usuario a guardar
   * @return el usuario guardado
   */
  UserEntity saveUser(UserEntity user);
}