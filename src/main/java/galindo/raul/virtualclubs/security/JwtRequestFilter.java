package galindo.raul.virtualclubs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import galindo.raul.virtualclubs.dtos.ApiResponse;
import galindo.raul.virtualclubs.dtos.ResponseType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro para validar JWT en cada petición.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/authenticate",
            "/api/auth/logout",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/request-password-reset",
            "/api/auth/reset-password-redirect",
            "/api/auth/google",
            "/actuator/health",
            "/actuator/info",
            "/"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        log.debug("[JWT] Procesando petición: {}", requestPath);

        try {
            String token = jwtService.extractToken(request);

            if (token != null && jwtService.isValidAccessToken(token)) {
                UsernamePasswordAuthenticationToken auth = jwtService.getAuthentication(token);
                if (auth != null) {
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("[JWT] Autenticación exitosa para {}", auth.getName());
                } else {
                    log.warn("[JWT] No se pudo obtener autenticación del token");
                    sendUnauthorized(response, "authentication_error");
                    return;
                }

            } else {
                log.warn("[JWT] Token inválido o expirado en petición {}", requestPath);
                sendUnauthorized(response, "access_token_invalid_or_expired");
                return;
            }

        } catch (Exception e) {
            log.error("[JWT] Error autenticando token: {}", e.getMessage(), e);
            sendUnauthorized(response, "authentication_error");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(),
                new ApiResponse<>(false, message, ResponseType.ERROR, null));
    }
}