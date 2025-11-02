package galindo.raul.virtualclubs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import galindo.raul.virtualclubs.dtos.ApiResponse;
import galindo.raul.virtualclubs.models.enums.ErrorType;
import galindo.raul.virtualclubs.models.enums.ResponseType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that validates JWT access tokens on each request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String token = jwtService.extractToken(request);

        if (token != null) {
            if (jwtService.isValidAccessToken(token)) {
                var auth = jwtService.getAuthentication(token);
                if (auth != null) {
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } else {
                log.warn("[JWT] Token inválido o expirado para {}", request.getRequestURI());
                sendUnauthorized(response, ErrorType.INVALID_ACCESS_TOKEN);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, ErrorType error) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(),
                new ApiResponse<>(false, error, ResponseType.ERROR, null));
    }
}