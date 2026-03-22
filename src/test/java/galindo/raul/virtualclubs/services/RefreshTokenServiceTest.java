package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.entities.RefreshToken;
import galindo.raul.virtualclubs.models.entities.User;
import galindo.raul.virtualclubs.repositories.RefreshTokenRepository;
import galindo.raul.virtualclubs.repositories.VirtualClubsUsersDetailsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RefreshTokenService.
 * Usa Mockito para simular los repositorios — no necesita base de datos.
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private VirtualClubsUsersDetailsRepository userRepository;

    @InjectMocks
    private RefreshTokenService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@test.com").build();
    }

    // ─────────────────────────────────────────────────────────────
    // createOrUpdateRefreshToken
    // ─────────────────────────────────────────────────────────────

    @Test
    void crear_usuarioSinTokenPrevio_guardaToken() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());

        service.createOrUpdateRefreshToken("user@test.com", "nuevo-token");

        verify(refreshTokenRepository).save(argThat(rt -> rt.getToken().equals("nuevo-token")));
    }

    @Test
    void crear_usuarioConTokenExistente_eliminaElViejoYGuardaElNuevo() {
        RefreshToken tokenExistente = RefreshToken.builder().token("token-viejo").user(user).build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(tokenExistente));

        service.createOrUpdateRefreshToken("user@test.com", "token-nuevo");

        verify(refreshTokenRepository).delete(tokenExistente);
        verify(refreshTokenRepository).save(argThat(rt -> rt.getToken().equals("token-nuevo")));
    }

    @Test
    void crear_usuarioNoExiste_lanzaExcepcion() {
        when(userRepository.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createOrUpdateRefreshToken("nadie@test.com", "token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ─────────────────────────────────────────────────────────────
    // deleteByToken
    // ─────────────────────────────────────────────────────────────

    @Test
    void eliminarPorToken_tokenExistente_loElimina() {
        RefreshToken rt = RefreshToken.builder().token("mi-token").user(user).build();
        when(refreshTokenRepository.findByToken("mi-token")).thenReturn(Optional.of(rt));

        service.deleteByToken("mi-token");

        verify(refreshTokenRepository).delete(rt);
    }

    @Test
    void eliminarPorToken_tokenInexistente_noHaceNada() {
        when(refreshTokenRepository.findByToken("no-existe")).thenReturn(Optional.empty());

        service.deleteByToken("no-existe");

        verify(refreshTokenRepository, never()).delete(any());
    }

    // ─────────────────────────────────────────────────────────────
    // deleteByUser
    // ─────────────────────────────────────────────────────────────

    @Test
    void eliminarPorUsuario_tokenExistente_loElimina() {
        RefreshToken rt = RefreshToken.builder().token("token").user(user).build();
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(rt));

        service.deleteByUser(user);

        verify(refreshTokenRepository).delete(rt);
    }

    @Test
    void eliminarPorUsuario_sinToken_noHaceNada() {
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());

        service.deleteByUser(user);

        verify(refreshTokenRepository, never()).delete(any());
    }

    // ─────────────────────────────────────────────────────────────
    // getEmailFromRefreshToken
    // ─────────────────────────────────────────────────────────────

    @Test
    void obtenerEmail_tokenValido_retornaEmail() {
        RefreshToken rt = RefreshToken.builder().token("token-valido").user(user).build();
        when(refreshTokenRepository.findByToken("token-valido")).thenReturn(Optional.of(rt));

        String email = service.getEmailFromRefreshToken("token-valido");

        assertThat(email).isEqualTo("user@test.com");
    }

    @Test
    void obtenerEmail_tokenInvalido_lanzaExcepcion() {
        when(refreshTokenRepository.findByToken("token-invalido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEmailFromRefreshToken("token-invalido"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid refresh token");
    }
}
