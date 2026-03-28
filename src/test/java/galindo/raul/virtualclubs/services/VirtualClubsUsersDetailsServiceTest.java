package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.entities.AuthProviderEntity;
import galindo.raul.virtualclubs.models.entities.RoleEntity;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.enums.Role;
import galindo.raul.virtualclubs.models.exceptions.EmailNotFoundException;
import galindo.raul.virtualclubs.models.exceptions.UserAlreadyExistException;
import galindo.raul.virtualclubs.repositories.UserEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UserEntityServiceImpl.
 * Usa Mockito para simular el repositorio y el PasswordEncoder — no necesita base de datos.
 */
@ExtendWith(MockitoExtension.class)
class VirtualClubsUsersDetailsServiceTest {

    @Mock
    private UserEntityRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserEntityServiceImpl service;

    private UserEntity userConProviderLocal;

    @BeforeEach
    void setUp() {
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
    // loadUserByUsername
    // ─────────────────────────────────────────────────────────────

    @Test
    void loadUserByUsername_usuarioExiste_retornaUserDetails() {
        when(userRepository.findByEmailWithRolesAndProviders("user@test.com")).thenReturn(Optional.of(userConProviderLocal));

        UserDetails details = service.loadUserByUsername("user@test.com");

        assertThat(details.getUsername()).isEqualTo("user@test.com");
        assertThat(details.getPassword()).isEqualTo("$2a$10$hashedpassword");
    }

    @Test
    void loadUserByUsername_usuarioNoExiste_lanzaEmailNotFoundException() {
        when(userRepository.findByEmailWithRolesAndProviders("ghost@test.com")).thenReturn(Optional.empty());

        // UserEntityServiceImpl lanza EmailNotFoundException (extends RuntimeException)
        // al no encontrar el usuario, antes de llegar a construir el UserDetails
        assertThatThrownBy(() -> service.loadUserByUsername("ghost@test.com"))
                .isInstanceOf(EmailNotFoundException.class);
    }

    @Test
    void loadUserByUsername_rolConPrefijo_noGeneraDobleROLE() {
        // El rol "USER" en la entidad no debe quedar "ROLE_ROLE_USER"
        when(userRepository.findByEmailWithRolesAndProviders("user@test.com")).thenReturn(Optional.of(userConProviderLocal));

        UserDetails details = service.loadUserByUsername("user@test.com");

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

        UserDetails details = service.loadUserByUsername("google@test.com");

        assertThat(details.getPassword()).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────
    // registerUser
    // ─────────────────────────────────────────────────────────────

    @Test
    void registerUser_emailNuevo_guardaYRetornaUsuario() {
        when(userRepository.findByEmail("nuevo@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Pass12")).thenReturn("$2a$hashed");
        when(userRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = service.registerUser("nuevo@test.com", "Pass12");

        assertThat(result.getEmail()).isEqualTo("nuevo@test.com");
        verify(userRepository).saveAndFlush(any());
    }

    @Test
    void registerUser_emailDuplicado_lanzaUserAlreadyExistException() {
        when(userRepository.findByEmail("dup@test.com")).thenReturn(Optional.of(userConProviderLocal));

        assertThatThrownBy(() -> service.registerUser("dup@test.com", "Pass12"))
                .isInstanceOf(UserAlreadyExistException.class);

        verify(userRepository, never()).saveAndFlush(any());
    }

    // ─────────────────────────────────────────────────────────────
    // registerOrLoadUserWithGoogle
    // ─────────────────────────────────────────────────────────────

    @Test
    void google_usuarioGoogleExistente_retornaMismoUsuarioSinGuardar() {
        when(userRepository.findByProvider("GOOGLE", "gid-123", "user@test.com"))
                .thenReturn(Optional.of(userConProviderLocal));

        UserEntity result = service.registerOrLoadUserWithGoogle("user@test.com", "Test User", "gid-123");

        assertThat(result).isSameAs(userConProviderLocal);
        verify(userRepository, never()).save(any());
    }

    @Test
    void google_usuarioLocalVerificado_vinculaProviderGoogle() {
        when(userRepository.findByProvider("GOOGLE", "gid-456", "user@test.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(userConProviderLocal));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = service.registerOrLoadUserWithGoogle("user@test.com", "Test User", "gid-456");

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

        service.registerOrLoadUserWithGoogle("unverified@test.com", "User", "gid-789");

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

        UserEntity result = service.registerOrLoadUserWithGoogle("new@test.com", "New User", "gid-new");

        assertThat(result.isEmailVerified()).isTrue();
        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(result.getAuthProviderEntities())
                .extracting("providerName")
                .containsExactly("GOOGLE");
    }
}
