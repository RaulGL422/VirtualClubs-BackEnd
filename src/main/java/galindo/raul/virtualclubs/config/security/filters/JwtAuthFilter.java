package galindo.raul.virtualclubs.config.security.filters;

import tools.jackson.databind.ObjectMapper;
import galindo.raul.virtualclubs.config.security.utils.JwtUtils;
import galindo.raul.virtualclubs.dtos.response.ApiResponse;
import galindo.raul.virtualclubs.models.enums.ErrorType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter responsible for intercepting HTTP requests to validate JWT tokens.
 * It extracts the token from the Authorization header and sets the security context
 * if the token is valid.
 *
 * <p>Si el header {@code Authorization: Bearer} está presente pero el token es inválido
 * o ha expirado, responde con HTTP 401 y el código de error {@link ErrorType#INVALID_TOKEN},
 * en lugar de dejar que Spring Security devuelva un 403 genérico sin formato ApiResponse.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtUtils jwtUtils;
  private final UserDetailsService userDetailsService;
  private final ObjectMapper objectMapper;

  /**
   * Filters incoming requests to check for a valid JWT in the Authorization header.
   *
   * @param request     The incoming HTTP request.
   * @param response    The outgoing HTTP response.
   * @param filterChain The chain of filters to execute.
   * @throws ServletException If a servlet-specific error occurs.
   * @throws IOException      If an I/O error occurs during filtering.
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);

      if (jwtUtils.isTokenValid(token, "access")) {
        String username = jwtUtils.getUsernameFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } else {
        log.warn("Bearer token inválido o expirado en request a {}", request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
            objectMapper.writeValueAsString(ApiResponse.error(ErrorType.INVALID_TOKEN))
        );
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}
