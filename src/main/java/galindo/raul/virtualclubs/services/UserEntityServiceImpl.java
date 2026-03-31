package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.entities.AuthProviderEntity;
import galindo.raul.virtualclubs.models.entities.RoleEntity;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.enums.Role;
import galindo.raul.virtualclubs.models.exceptions.EmailNotFoundException;
import galindo.raul.virtualclubs.models.exceptions.UserAlreadyExistException;
import galindo.raul.virtualclubs.repositories.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of the UserDetailsService for Spring Security and user management operations.
 * Handles registration, authentication provider linking, and user retrieval.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserEntityServiceImpl implements UserDetailsService {

  private final UserEntityRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  
  @Override
  @Transactional
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    UserEntity user = userRepository.findByEmailWithRolesAndProviders(email)
        .orElseThrow(() -> new EmailNotFoundException(email));
    
    // Add authorities
    Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
    user.getRoles().forEach(role -> grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_".concat(role.getRole().name()))));
    user.getRoles().stream()
        .flatMap(role -> role.getPermissionList().stream())
        .forEach(permission -> grantedAuthorities.add(new SimpleGrantedAuthority(permission.getName())));
    
    String password = user.getAuthProviderEntities().stream()
        .filter(ap -> "LOCAL".equalsIgnoreCase(ap.getProviderName()))
        .findFirst()
        .map(AuthProviderEntity::getPasswordHash)
        .orElse("");
    
    return new User(
        user.getEmail(),
        password,
        grantedAuthorities
    );
  }
  
  /**
   * Registers a new user with a LOCAL authentication provider.
   * @param email User's email.
   * @param password Plain text password to be encoded.
   * @return The saved UserEntity.
   */
  @Transactional
  public UserEntity registerUser(String email, String password) {
      // Verify if user exists
    if (userRepository.findByEmail(email).isPresent()) {
      throw new UserAlreadyExistException(email);
    }
    
    // Create new user
    AuthProviderEntity authProvider = AuthProviderEntity.builder()
        .providerName("LOCAL")
        .passwordHash(passwordEncoder.encode(password))
        .build();
    
    UserEntity newUser = UserEntity.builder()
        .email(email)
        .build();

    newUser.addAuthProvider(authProvider);
    newUser.addRole(RoleEntity.builder().role(Role.USER).build());
    userRepository.saveAndFlush(newUser);
    
    return newUser;
  }
  
  /**
   * Handles Google OAuth2 user logic: logs in existing users, links Google to verified local accounts,
   * or creates new accounts.
   * @param email User's email from Google.
   * @param name User's name from Google.
   * @param googleId Unique Google identifier.
   * @return The processed UserEntity.
   */
  @Transactional
  public UserEntity registerOrLoadUserWithGoogle(String email, String name, String googleId) {
    log.info("Processing Google login/register for email='{}'", email);
    
    // 1. Buscar usuario con Google provider
    Optional<UserEntity> googleUserOpt = userRepository.findByProvider("GOOGLE", googleId, email);
    if (googleUserOpt.isPresent()) {
      log.info("Found existing Google user '{}'", email);
      return googleUserOpt.get();
    }
    
    // 2. Buscar usuario con LOCAL
    Optional<UserEntity> localUserOpt = userRepository.findByEmail(email);
    if (localUserOpt.isPresent()) {
      UserEntity localUser = localUserOpt.get();
      if (localUser.isEmailVerified()) {
        log.info("Linking Google provider to existing verified LOCAL account '{}'", email);
        addGoogleProvider(localUser, googleId);
        return userRepository.save(localUser);
      } else {
        log.warn("Local account '{}' is unverified, deleting and creating Google account", email);
        userRepository.delete(localUser);
        userRepository.flush();
      }
    }
    
    // 3. Crear nuevo usuario con Google
    log.info("Creating new Google account for email='{}'", email);
    UserEntity newUser = createGoogleUser(email, name, googleId);
    return userRepository.save(newUser);
  }
  
  // -------------------------------
  // Métodos auxiliares
  // -------------------------------
  
  private void addGoogleProvider(UserEntity user, String googleId) {
    AuthProviderEntity googleProvider = AuthProviderEntity.builder()
        .providerName("GOOGLE")
        .providerUserId(googleId)
        .user(user)
        .build();
    user.addAuthProvider(googleProvider);
  }
  
  /**
   * Finds a user by email returning an Optional.
   * @param email The email to search for.
   * @return Optional containing the user if found.
   */
  public Optional<UserEntity> findByEmailOptional(String email) {
    return userRepository.findByEmail(email);
  }
  
  private UserEntity createGoogleUser(String email, String name, String googleId) {
    UserEntity user = UserEntity.builder()
        .email(email)
        .name(name)
        .emailVerified(true)
        .build();
    
    addGoogleProvider(user, googleId);
    user.addRole(RoleEntity.builder().role(Role.USER).build());
    return user;
  }
  
  /**
   * Retrieves a user by email or throws an exception if not found.
   * @param email The email to search for.
   * @return The found UserEntity.
   */
  public UserEntity getUserFromEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new EmailNotFoundException(email));
  }

  /**
   * Persiste cambios en un UserEntity existente.
   * @param user el usuario a guardar
   * @return el usuario guardado
   */
  @Transactional
  public UserEntity saveUser(UserEntity user) {
    return userRepository.save(user);
  }
}
