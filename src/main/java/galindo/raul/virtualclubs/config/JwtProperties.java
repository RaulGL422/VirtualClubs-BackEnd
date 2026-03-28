package galindo.raul.virtualclubs.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Typed configuration properties for JWT token generation.
 * Bound to properties prefixed with {@code jwt} in {@code application.properties}.
 * Validated at application startup — missing or blank values cause a fast fail.
 */
@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    @NotBlank String secret,
    @Positive long expiration,
    @Positive long refreshExpiration
) {}
