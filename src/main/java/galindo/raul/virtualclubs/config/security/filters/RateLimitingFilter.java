package galindo.raul.virtualclubs.config.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import galindo.raul.virtualclubs.dtos.response.ApiResponse;
import galindo.raul.virtualclubs.models.enums.ErrorType;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Filtro de rate limiting por IP y endpoint.
 * Los endpoints protegidos y sus límites se configuran en application.properties
 * bajo el prefijo "rate-limiting.limits". Los endpoints no presentes en el mapa
 * pasan sin restricción.
 *
 * Cada IP tiene su propio bucket por endpoint: una IP que agota su cuota en /login
 * no afecta a otra IP ni al bucket del mismo usuario en /register.
 */
public class RateLimitingFilter implements Filter {

  private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper;
  private final Map<String, Integer> limits;

  public RateLimitingFilter(ObjectMapper objectMapper, Map<String, Integer> limits) {
    this.objectMapper = objectMapper;
    this.limits = limits;
  }

  /** Extrae la IP real teniendo en cuenta proxies (usa el primer valor de X-Forwarded-For). */
  private String extractClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  /** Devuelve el bucket para ip:endpoint, creándolo la primera vez con la capacidad configurada. */
  private Bucket resolveBucket(String ip, String path, int capacity) {
    return buckets.computeIfAbsent(ip + ":" + path, _ ->
        Bucket.builder()
            .addLimit(Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(capacity, Duration.ofMinutes(1))
                .build())
            .build()
    );
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String path = httpRequest.getRequestURI();
    Integer capacity = limits.get(path);

    // Si el endpoint no está en el mapa de límites, pasa sin restricción
    if (capacity == null) {
      chain.doFilter(request, response);
      return;
    }

    String clientIp = extractClientIp(httpRequest);
    Bucket bucket = resolveBucket(clientIp, path, capacity);

    if (bucket.tryConsume(1)) {
      chain.doFilter(request, response);
    } else {
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      httpResponse.setStatus(429);
      httpResponse.setContentType("application/json");
      httpResponse.setCharacterEncoding("UTF-8");
      httpResponse.getWriter().write(
          objectMapper.writeValueAsString(ApiResponse.error(ErrorType.RATE_LIMIT_EXCEEDED))
      );
    }
  }
}
