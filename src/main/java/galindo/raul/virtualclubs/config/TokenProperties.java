package galindo.raul.virtualclubs.config;

import galindo.raul.virtualclubs.models.enums.TokenType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración del ciclo de vida de los tokens de un solo uso.
 * Los valores se leen de {@code application.properties} bajo el prefijo {@code app.token}.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.token")
public class TokenProperties {

    /** Minutos hasta que expira un token de verificación de email. Por defecto 1440 (24 h). */
    private int verificationExpiry = 1440;

    /** Minutos hasta que expira un token de reset de contraseña. Por defecto 30. */
    private int resetExpiry = 30;

    public int getExpiryMinutes(TokenType type) {
        return switch (type) {
            case EMAIL_VERIFICATION -> verificationExpiry;
            case PASSWORD_RESET     -> resetExpiry;
        };
    }
}