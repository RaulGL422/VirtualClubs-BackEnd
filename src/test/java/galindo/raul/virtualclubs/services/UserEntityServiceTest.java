package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.entities.AuthProviderEntity;
import galindo.raul.virtualclubs.models.entities.RoleEntity;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.enums.Role;
import galindo.raul.virtualclubs.models.exceptions.EmailNotFoundException;
import galindo.raul.virtualclubs.models.exceptions.UserAlreadyExistException;
import galindo.raul.virtualclubs.repositories.RoleEntityRepository;
import galindo.raul.virtualclubs.repositories.UserEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UserEntityServiceImpl.
 * Sin contexto Spring — Mockito puro.
 */
@ExtendWith(MockitoExtension.class)
class UserEntityServiceTest {

    @Mock private UserEntityRepository userRepository;
    @Mock private RoleEntityRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserEntityServiceImpl userService;

    private static final String EMAIL    = "service@test.com";
    private static final String PASSWORD = "PAss12";

    private UserEntity userConProviderLocal;

    @BeforeEach
    void setUp() {
        lenient().when(roleRepository.findByRole(Role.USER))
                .thenReturn(Optional.of(RoleEntity.builder().role(Role.USER).build()));

        AuthProviderEntity localProvider = AuthProviderEntity.builder()
                .providerName("LOCAL")
                .passwordHash("$2a$10$hashedpassword")
                .build();

        userConProviderLocal = UserEntity.builder()
                .id(1L)
                .email("user@test.com")
                .emailVerified(true)
                .build();

        userConProviderLocal.addAuthProvider(localProvider);
        userConProviderLocal.addRole(RoleEntity.builder().role(Role.USER).build());
    }

    // ─────────────────────────────────────────────────────────────
    // registerUser
    // ─────────────────────────────────────────────────────────────

    @Test
    void registerUser_emailNuevo_creaUserConProviderLocal() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn("hashed");
        when(userRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.registerUser(EMAIL, PASSWORD);

        assertThat(result.getEmail()).isEqualTo(EMAIL);
        assertThat(result.getAuthProviderEntities()).hasSize(1);
        assertThat(result.getAuthProviderEntities().iterator().next().getProviderName())
                .isEqualToIgnoringCase("LOCAL");
        assertThat(result.getAuthProviderEntities().iterator().next().getPasswordHash())
                .isEqualTo("hashed");
    }

    @Test
    void registerUser_emailNuevo_asignaRolUser() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.registerUser(EMAIL, PASSWORD);

        assertThat(result.getRoles()).hasSize(1);
        assertThat(result.getRoles().iterator().next().getRole().name()).isEqualTo("USER");
    }

    @Test
    void registerUser_emailNuevo_noAsignaRolDuplicado() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.registerUser(EMAIL, PASSWORD);

        // Exactamente 1 rol — no el bug de duplicación anterior
        assertThat(result.getRoles()).hasSize(1);
    }

    @Test
    void registerUser_emailDuplicado_lanzaUserAlreadyExistException() {
        when(userRepository.findByEmail("dup@test.com")).thenReturn(Optional.of(userConProviderLocal));

        assertThatThrownBy(() -> userService.registerUser("dup@test.com", PASSWORD))
                .isInstanceOf(UserAlreadyExistException.class);

        verify(userRepository, never()).saveAndFlush(any());
    }

    // ─────────────────────────────────────────────────────────────
    // loadUserByUsername
    // ─────────────────────────────────────────────────────────────

    @Test
    void loadUserByUsername_sinRolesNiProviders_passwordYAuthoritiesVacios() {
        UserEntity user = UserEntity.builder().id(1L).email(EMAIL).build();
        when(userRepository.findByEmailWithRolesAndProviders(EMAIL)).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername(EMAIL);

        assertThat(details.getUsername()).isEqualTo(EMAIL);
        // Sin roles ni providers → sin authorities, password vacío
        assertThat(details.getPassword()).isEmpty();
        assertThat(details.getAuthorities()).isEmpty();
    }

    @Test
    void loadUserByUsername_conProviderLocal_retornaPasswordHash() {
        when(userRepository.findByEmailWithRolesAndProviders("user@test.com")).thenReturn(Optional.of(userConProviderLocal));

        UserDetails details = userService.loadUserByUsername("user@test.com");

        assertThat(details.getUsername()).isEqualTo("user@test.com");
        assertThat(details.getPassword()).isEqualTo("$2a$10$hashedpassword");
    }

    @Test
    void loadUserByUsername_rolConPrefijo_noGeneraDobleROLE() {
        // El rol "USER" en la entidad no debe quedar "ROLE_ROLE_USER"
        when(userRepository.findByEmailWithRolesAndProviders("user@test.com")).thenReturn(Optional.of(userConProviderLocal));

        UserDetails details = userService.loadUserByUsername("user@test.com");

        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER")
                .doesNotContain("ROLE_ROLE_USER");
    }

    @Test
    void loadUserByUsername_sinProviderLocal_passwordEsVacia() {
        // Usuario Google sin contraseña local → password debe ser ""
        UserEntity googleUser = UserEntity.builder()
                .id(2L)
                .email("google@test.com")
                .emailVerified(true)
                .build();
        googleUser.addAuthProvider(AuthProviderEntity.builder()
                .providerName("GOOGLE")
                .providerUserId("google-id-123")
                .build());

        when(userRepository.findByEmailWithRolesAndProviders("google@test.com")).thenReturn(Optional.of(googleUser));

        UserDetails details = userService.loadUserByUsername("google@test.com");

        assertThat(details.getPassword()).isEmpty();
    }

    @Test
    void loadUserByUsername_emailInexistente_lanzaEmailNotFoundException() {
        when(userRepository.findByEmailWithRolesAndProviders(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername(EMAIL))
                .isInstanceOf(EmailNotFoundException.class);
    }

    // ─────────────────────────────────────────────────────────────
    // getUserFromEmail
    // ─────────────────────────────────────────────────────────────

    @Test
    void getUserFromEmail_emailExistente_retornaUser() {
        UserEntity user = UserEntity.builder().id(1L).email(EMAIL).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        UserEntity result = userService.getUserFromEmail(EMAIL);

        assertThat(result.getEmail()).isEqualTo(EMAIL);
    }

    @Test
    void getUserFromEmail_emailInexistente_lanzaEmailNotFoundException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserFromEmail(EMAIL))
                .isInstanceOf(EmailNotFoundException.class);
    }

    // ─────────────────────────────────────────────────────────────
    // findByEmailOptional
    // ─────────────────────────────────────────────────────────────

    @Test
    void findByEmailOptional_emailExistente_retornaPresente() {
        UserEntity user = UserEntity.builder().id(1L).email(EMAIL).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertThat(userService.findByEmailOptional(EMAIL)).isPresent();
    }

    @Test
    void findByEmailOptional_emailInexistente_retornaVacio() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThat(userService.findByEmailOptional(EMAIL)).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────
    // saveUser
    // ─────────────────────────────────────────────────────────────

    @Test
    void saveUser_delegaAlRepositorio() {
        UserEntity user = UserEntity.builder().id(1L).email(EMAIL).build();
        when(userRepository.save(user)).thenReturn(user);

        UserEntity result = userService.saveUser(user);

        verify(userRepository).save(user);
        assertThat(result).isSameAs(user);
    }

    // ─────────────────────────────────────────────────────────────
    // registerOrLoadUserWithGoogle
    // ─────────────────────────────────────────────────────────────

    @Test
    void google_usuarioGoogleExistente_retornaMismoUsuarioSinGuardar() {
        when(userRepository.findByProvider("GOOGLE", "gid-123", "user@test.com"))
                .thenReturn(Optional.of(userConProviderLocal));

        UserEntity result = userService.registerOrLoadUserWithGoogle("user@test.com", "Test User", "gid-123");

        assertThat(result).isSameAs(userConProviderLocal);
        verify(userRepository, never()).save(any());
    }

    @Test
    void google_usuarioLocalVerificado_vinculaProviderGoogle() {
        when(userRepository.findByProvider("GOOGLE", "gid-456", "user@test.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(userConProviderLocal));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.registerOrLoadUserWithGoogle("user@test.com", "Test User", "gid-456");

        assertThat(result.getAuthProviderEntities())
                .extracting("providerName")
                .contains("GOOGLE", "LOCAL");
    }

    @Test
    void google_usuarioLocalNoVerificado_eliminaYCreaUsuarioGoogle() {
        UserEntity noVerificado = UserEntity.builder()
                .id(3L)
                .email("unverified@test.com")
                .emailVerified(false)
                .build();

        when(userRepository.findByProvider("GOOGLE", "gid-789", "unverified@test.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("unverified@test.com")).thenReturn(Optional.of(noVerificado));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.registerOrLoadUserWithGoogle("unverified@test.com", "User", "gid-789");

        verify(userRepository).delete(noVerificado);
        verify(userRepository).save(argThat(u ->
                u.isEmailVerified() && u.getEmail().equals("unverified@test.com")));
    }

    @Test
    void google_usuarioNuevo_creaUsuarioVerificadoConProviderGoogle() {
        when(userRepository.findByProvider("GOOGLE", "gid-new", "new@test.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.registerOrLoadUserWithGoogle("new@test.com", "New User", "gid-new");

        assertThat(result.isEmailVerified()).isTrue();
        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(result.getAuthProviderEntities())
                .extracting("providerName")
                .containsExactly("GOOGLE");
    }
}
