package galindo.raul.virtualclubs.config.security.filters;

import tools.jackson.databind.ObjectMapper;
import galindo.raul.virtualclubs.dtos.request.AuthRequest;
import galindo.raul.virtualclubs.models.Tokens;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.enums.ErrorType;
import galindo.raul.virtualclubs.services.TokensService;
import galindo.raul.virtualclubs.services.UserService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de JwtAuthLoginFilter — el filtro que emite tokens en el login.
 *
 * Solo estaba cubierto indirectamente por tests de integración. Cubre las tres
 * ramas del filtro:
 *   1. attemptAuthentication con JSON malformado → AuthenticationServiceException.
 *   2. unsuccessfulAuthentication → 401 + código INVALID_CREDENTIALS.
 *   3. successfulAuthentication (aislado) → header Authorization + tokens en el body.
 *
 * Usa mocks de UserService/TokensService/AuthenticationManager para aislar el filtro
 * sin contexto Spring, igual que JwtAuthFilterTest.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthLoginFilterTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserService userService;
    @Mock private TokensService tokensService;
    @Mock private FilterChain chain;

    private JwtAuthLoginFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthLoginFilter(authenticationManager, userService, tokensService, new ObjectMapper());
    }

    // ─────────────────────────────────────────────────────────────
    // attemptAuthentication
    // ─────────────────────────────────────────────────────────────

    @Test
    void attemptAuthentication_conJsonMalformado_lanzaAuthenticationServiceException() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContent("{ esto no es json valido".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse res = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.attemptAuthentication(req, res))
                .isInstanceOf(AuthenticationServiceException.class);

        verifyNoInteractions(authenticationManager);
    }

    @Test
    void attemptAuthentication_conCredencialesValidas_delegaAlAuthenticationManager() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContent(new ObjectMapper().writeValueAsBytes(new AuthRequest("user@test.com", "password123")));
        MockHttpServletResponse res = new MockHttpServletResponse();

        Authentication expected = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(expected);

        Authentication result = filter.attemptAuthentication(req, res);

        assertThat(result).isSameAs(expected);
    }

    // ─────────────────────────────────────────────────────────────
    // unsuccessfulAuthentication
    // ─────────────────────────────────────────────────────────────

    @Test
    void unsuccessfulAuthentication_retorna401ConCodigoCredencialesInvalidas() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.unsuccessfulAuthentication(req, res, new BadCredentialsException("credenciales invalidas"));

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(res.getContentType()).contains("application/json");
        assertThat(res.getContentAsString()).contains("\"success\":false");
        assertThat(res.getContentAsString())
                .contains(String.valueOf(ErrorType.INVALID_CREDENTIALS.getCode())); // "2"
    }

    // ─────────────────────────────────────────────────────────────
    // successfulAuthentication
    // ─────────────────────────────────────────────────────────────

    @Test
    void successfulAuthentication_agregaHeaderAuthorizationYRetornaTokens() throws Exception {
        UserDetails principal = User.withUsername("user@test.com").password("").roles("USER").build();
        Authentication authResult = mock(Authentication.class);
        when(authResult.getPrincipal()).thenReturn(principal);

        UserEntity userEntity = UserEntity.builder().id(1L).email("user@test.com").build();
        when(userService.getUserFromEmail("user@test.com")).thenReturn(userEntity);
        when(tokensService.getNewTokens(any(), any()))
                .thenReturn(new Tokens("access-token-123", "refresh-token-456"));

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.successfulAuthentication(req, res, chain, authResult);

        assertThat(res.getHeader("Authorization")).isEqualTo("access-token-123");
        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(res.getContentType()).contains("application/json");
        assertThat(res.getContentAsString()).contains("access-token-123");
        assertThat(res.getContentAsString()).contains("refresh-token-456");
    }
}
