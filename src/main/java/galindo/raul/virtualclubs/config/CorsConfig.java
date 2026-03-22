package galindo.raul.virtualclubs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class to handle Cross-Origin Resource Sharing (CORS) settings.
 * This ensures that the backend can communicate with the frontend hosted on different origins.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * List of allowed origins injected from the application properties.
     */
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * Configures CORS mappings for the application.
     *
     * @param registry the CorsRegistry to add mappings to.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}