package galindo.raul.virtualclubs.config.security.filters;

import galindo.raul.virtualclubs.config.security.utils.JwtUtils;
import galindo.raul.virtualclubs.services.UserEntityServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter responsible for intercepting HTTP requests to validate JWT tokens.
 * It extracts the token from the Authorization header and sets the security context
 * if the token is valid.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtUtils jwtUtils;
  private final UserEntityServiceImpl userEntityService;
  
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
      // Obtain token JWT
      String token = header.substring(7);
      
      if (jwtUtils.isTokenValid(token, "access")) {
        String username = jwtUtils.getUsernameFromToken(token);
        UserDetails userDetails = userEntityService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }
    
    filterChain.doFilter(request, response);
  }
}
