package galindo.raul.virtualclubs.config.security.filters;

import tools.jackson.databind.ObjectMapper;
import galindo.raul.virtualclubs.config.security.utils.JwtUtils;
import galindo.raul.virtualclubs.models.enums.ErrorType;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de JwtAuthFilter.
 *
 * Cubre los tres caminos posibles del filtro:
 *   1. Sin header Authorization → el filtro no actúa y pasa al siguiente.
 *   2. Bearer token válido → SecurityContext se popula y la cadena continúa.
 *   3. Bearer token inválido → 401 + código 4 (INVALID_TOKEN) y la cadena se corta.
 *
 * Usa mocks de JwtUtils y UserDetailsService para aislar el filtro sin Spring context.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock private JwtUtils jwtUtils;
    @Mock private UserDetailsService userDetailsService;
    @Mock private FilterChain chain;

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(jwtUtils, userDetailsService, new ObjectMapper());
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ─────────────────────────────────────────────────────────────
    // Sin header Authorization
    // ─────────────────────────────────────────────────────────────

    @Test
    void sinHeaderAuthorization_continúaAlSiguienteFiltroSinTocarJwt() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtUtils);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void headerBasicAuth_esIgnoradoYContinúaCadena() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verifyNoInteractions(jwtUtils);
    }

    // ─────────────────────────────────────────────────────────────
    // Bearer token válido
    // ─────────────────────────────────────────────────────────────

    @Test
    void tokenValido_populaSecurityContextYContinúaCadena() throws Exception {
        UserDetails userDetails = User.withUsername("user@test.com").password("").roles("USER").build();
        when(jwtUtils.isTokenValid("valid-token", "access")).thenReturn(true);
        when(jwtUtils.getUsernameFromToken("valid-token")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("user@test.com");
    }

    @Test
    void tokenValido_noModificaStatusDeRespuesta() throws Exception {
        UserDetails userDetails = User.withUsername("user@test.com").password("").roles("USER").build();
        when(jwtUtils.isTokenValid("valid-token", "access")).thenReturn(true);
        when(jwtUtils.getUsernameFromToken("valid-token")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(200);
    }

    // ─────────────────────────────────────────────────────────────
    // Bearer token inválido / expirado
    // ─────────────────────────────────────────────────────────────

    @Test
    void tokenInvalido_retorna401ConErrorCode4() throws Exception {
        when(jwtUtils.isTokenValid("bad-token", "access")).thenReturn(false);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(res.getContentType()).contains("application/json");
        assertThat(res.getContentAsString()).contains("\"success\":false");
        assertThat(res.getContentAsString())
                .contains(String.valueOf(ErrorType.INVALID_TOKEN.getCode())); // "4"
    }

    @Test
    void tokenInvalido_cortaLaCadenaDeFiltroDespuesDe401() throws Exception {
        when(jwtUtils.isTokenValid("bad-token", "access")).thenReturn(false);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, chain);

        verifyNoInteractions(chain);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void tokenInvalido_noPopulaSecurityContext() throws Exception {
        when(jwtUtils.isTokenValid("expired-token", "access")).thenReturn(false);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer expired-token");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
