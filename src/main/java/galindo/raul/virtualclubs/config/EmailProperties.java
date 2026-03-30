package galindo.raul.virtualclubs.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración del servicio de envío de email (remitente).
 * Los valores se leen de {@code application.properties} bajo el prefijo {@code app.mail}.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.mail")
public class EmailProperties {

    private String from;
    private String fromName;
}