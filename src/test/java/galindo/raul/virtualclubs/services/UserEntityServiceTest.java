package galindo.raul.virtualclubs.services;

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
import java.util.Set;

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

    @BeforeEach
    void setUp() {
        lenient().when(roleRepository.findByRole(Role.USER))
                .thenReturn(Optional.of(RoleEntity.builder().role(Role.USER).build()));
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
        UserEntity existing = UserEntity.builder().id(1L).email(EMAIL).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.registerUser(EMAIL, PASSWORD))
                .isInstanceOf(UserAlreadyExistException.class);

        verify(userRepository, never()).saveAndFlush(any());
    }

    // ─────────────────────────────────────────────────────────────
    // loadUserByUsername
    // ─────────────────────────────────────────────────────────────

    @Test
    void loadUserByUsername_emailExistente_retornaUserDetails() {
        UserEntity user = UserEntity.builder().id(1L).email(EMAIL).build();
        when(userRepository.findByEmailWithRolesAndProviders(EMAIL)).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername(EMAIL);

        assertThat(details.getUsername()).isEqualTo(EMAIL);
        // Sin roles ni providers → sin authorities, password vacío
        assertThat(details.getPassword()).isEmpty();
        assertThat(details.getAuthorities()).isEmpty();
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
}