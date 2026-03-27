package galindo.raul.virtualclubs.config.security.filters.models;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Propiedades de rate limiting cargadas desde application.properties.
 *
 * Formato en properties:
 *   rate-limiting.limits[/v1/auth/login]=5
 *   rate-limiting.limits[/v1/auth/register]=3
 *
 * La clave es la ruta del endpoint y el valor es el número máximo
 * de peticiones permitidas por minuto por IP. Los endpoints no presentes
 * en el mapa no tienen restricción de rate limiting.
 */
@ConfigurationProperties(prefix = "rate-limiting")
public record RateLimitingProperties(Map<String, Integer> limits) {}
