package galindo.raul.virtualclubs.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

  private static final String BEARER_SCHEME = "bearerAuth";

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Virtual Clubs API")
            .description("""
                REST API para la gestión de clubes deportivos virtuales.

                ## Autenticación
                Los endpoints protegidos requieren un `Bearer` token JWT en el header `Authorization`.
                Obtén el token con `POST /v1/auth/login` o `POST /v1/auth/register`.

                ## Códigos de error
                Los errores se devuelven con un código numérico en el campo `message`:
                `1` interno · `2` credenciales · `3` refresh token · `4` token inválido ·
                `5` usuario no encontrado · `6` email duplicado · `7` campo vacío ·
                `8` email inválido · `9` contraseña corta · `10` contraseña débil ·
                `11` email no verificado · `12` sin proveedor local · `13` rate limit
                """)
            .version("0.1.0")
            .contact(new Contact()
                .name("Raúl Galindo")
                .url("https://rgal.dev"))
            .license(new License()
                .name("MIT")
                .url("https://github.com/RaulGL422/backend/blob/main/LICENSE")))
        .servers(List.of(
            new Server().url("https://api-vc.rgal.dev").description("Producción"),
            new Server().url("http://localhost:4584").description("Local")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
        .components(new Components()
            .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                .name(BEARER_SCHEME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Token JWT obtenido en /v1/auth/login o /v1/auth/register")));
  }
}
