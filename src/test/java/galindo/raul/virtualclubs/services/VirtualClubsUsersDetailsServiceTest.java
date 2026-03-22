package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.entities.AuthProvider;
import galindo.raul.virtualclubs.models.entities.User;
import galindo.raul.virtualclubs.repositories.VirtualClubsUsersDetailsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para VirtualClubsUsersDetailsService.
 * Usa Mockito para simular el repositorio — no necesita base de datos.
 */
@ExtendWith(MockitoExtension.class)
class VirtualClubsUsersDetailsServiceTest {

    @Mock
    private VirtualClubsUsersDetailsRepository userRepository;

    @InjectMocks
    private VirtualClubsUsersDetailsService service;

    private User userConProviderLocal;

    @BeforeEach
    void setUp() {
        userConProviderLocal = User.builder()
                .id(1L)
                .email("user@test.com")
                .roles(new HashSet<>(Set.of("ROLE_USER")))
                .emailVerified(true)
                .build();

        AuthProvider localProvider = AuthProvider.builder()
                .providerName("LOCAL")
                .passwordHash("$2a$10$hashedpassword")
                .build();

        userConProviderLocal.addAuthProvider(localProvider);
    }

    // ─────────────────────────────────────────────────────────────
    // loadUserByUsername
    // ─────────────────────────────────────────────────────────────

    @Test
    void loadUserByUsername_usuarioExiste_retornaUserDetails() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(userConProviderLocal));

        UserDetails details = service.loadUserByUsername("user@test.com");

        assertThat(details.getUsername()).isEqualTo("user@test.com");
        assertThat(details.getPassword()).isEqualTo("$2a$10$hashedpassword");
    }

    @Test
    void loadUserByUsername_usuarioNoExiste_lanzaUsernameNotFoundException() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost@test.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_rolSinPrefijo_agrega_ROLE_() {
        // El rol "ADMIN" (sin ROLE_) debe transformarse en "ROLE_ADMIN"
        User adminUser = User.builder()
                .id(2L)
                .email("admin@test.com")
                .roles(new HashSet<>(Set.of("ADMIN")))
                .emailVerified(true)
                .build();
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));

        UserDetails details = service.loadUserByUsername("admin@test.com");

        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_rolConPrefijo_noDuplica_ROLE_() {
        // El rol "ROLE_USER" ya tiene prefijo — no debe quedar "ROLE_ROLE_USER"
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(userConProviderLocal));

        UserDetails details = service.loadUserByUsername("user@test.com");

        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER")
                .doesNotContain("ROLE_ROLE_USER");
    }

    @Test
    void loadUserByUsername_sinProviderLocal_passwordEsVacia() {
        // Usuario Google sin contraseña local → password debe ser ""
        User googleUser = User.builder()
                .id(3L)
                .email("google@test.com")
                .roles(new HashSet<>(Set.of("ROLE_USER")))
                .emailVerified(true)
                .build();
        googleUser.addAuthProvider(AuthProvider.builder()
                .providerName("GOOGLE")
                .providerUserId("google-id-123")
                .build());
        when(userRepository.findByEmail("google@test.com")).thenReturn(Optional.of(googleUser));

        UserDetails details = service.loadUserByUsername("google@test.com");

        assertThat(details.getPassword()).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────
    // registerOrLoadUserWithGoogle
    // ─────────────────────────────────────────────────────────────

    @Test
    void google_usuarioGoogleExistente_retornaMismoUsuario() {
        when(userRepository.findByProvider("GOOGLE", "gid-123", "user@test.com"))
                .thenReturn(Optional.of(userConProviderLocal));

        User result = service.registerOrLoadUserWithGoogle("user@test.com", "Test User", "gid-123");

        assertThat(result).isSameAs(userConProviderLocal);
        verify(userRepository, never()).save(any());
    }

    @Test
    void google_usuarioLocalVerificado_vinculaProviderGoogle() {
        when(userRepository.findByProvider("GOOGLE", "gid-456", "user@test.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(userConProviderLocal));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = service.registerOrLoadUserWithGoogle("user@test.com", "Test User", "gid-456");

        assertThat(result.getAuthProviders())
                .extracting("providerName")
                .contains("GOOGLE", "LOCAL");
    }

    @Test
    void google_usuarioLocalNoVerificado_eliminaYCreaUsuarioGoogle() {
        User noVerificado = User.builder()
                .id(4L)
                .email("unverified@test.com")
                .roles(new HashSet<>())
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
    void google_usuarioNuevo_creaUsuarioVerificado() {
        when(userRepository.findByProvider("GOOGLE", "gid-new", "new@test.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = service.registerOrLoadUserWithGoogle("new@test.com", "New User", "gid-new");

        assertThat(result.isEmailVerified()).isTrue();
        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(result.getAuthProviders())
                .extracting("providerName")
                .containsExactly("GOOGLE");
    }
}
