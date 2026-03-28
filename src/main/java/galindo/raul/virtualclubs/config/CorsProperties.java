package galindo.raul.virtualclubs.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Typed configuration properties for CORS settings.
 * Bound to properties prefixed with {@code app.cors} in {@code application.properties}.
 * Validated at application startup — a null list causes a fast fail.
 */
@Validated
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
    @NotNull String[] allowedOrigins
) {}
