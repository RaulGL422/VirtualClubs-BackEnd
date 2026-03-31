package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.config.TokenProperties;
import galindo.raul.virtualclubs.dtos.request.EmailRequest;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.enums.TokenType;
import galindo.raul.virtualclubs.utils.AuthUrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;

/**
 * Servicio que centraliza el envío de notificaciones por email.
 *
 * Responsabilidades:
 *  - Crear el token de un solo uso para cada tipo de notificación
 *  - Construir la URL de acción (verificación / reset)
 *  - Resolver el asunto del email según el idioma (Locale)
 *  - Delegar el envío a {@link EmailService}
 *
 * El controlador solo llama a los métodos de alto nivel de esta clase,
 * sin conocer detalles de tokens, URLs ni plantillas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserTokenService userTokenService;
    private final EmailService emailService;
    private final TokenProperties tokenProperties;
    private final MessageSource messageSource;

    @Value("${app.api-base-url}")
    private String apiBaseUrl;

    /**
     * Genera un token de verificación de email y envía el correo al usuario.
     *
     * <p>La creación del token es transaccional. El envío del email es asíncrono
     * ({@link EmailService} usa {@code @Async}) y ocurre fuera de la transacción,
     * por lo que un fallo de envío no revierte el token — el usuario puede solicitar reenvío.
     *
     * @param user   usuario destinatario
     * @param locale idioma del email (viene del header Accept-Language del request)
     */
    @Transactional
    public void sendVerificationEmail(UserEntity user, Locale locale) {
        String token     = userTokenService.createTokenFor(user, TokenType.EMAIL_VERIFICATION);
        String verifyUrl = AuthUrlUtils.verifyEmailUrl(apiBaseUrl, token);
        String name     = displayName(user);
        int    expiry   = tokenProperties.getVerificationExpiry() / 60; // minutos → horas

        emailService.sendEmail(new EmailRequest(
            user.getEmail(),
            name,
            messageSource.getMessage("email.verify.subject", null, locale),
            "verify-email",
            Map.of("name", name, "verifyUrl", verifyUrl, "expiryHours", expiry),
            locale
        ));
        log.info("Email de verificación enviado a '{}'", user.getEmail());
    }

    /**
     * Genera un token de reset de contraseña y envía el correo al usuario.
     *
     * <p>Ver {@link #sendVerificationEmail} para la nota sobre transaccionalidad y envío asíncrono.
     *
     * @param user   usuario destinatario
     * @param locale idioma del email
     */
    @Transactional
    public void sendPasswordResetEmail(UserEntity user, Locale locale) {
        String token    = userTokenService.createTokenFor(user, TokenType.PASSWORD_RESET);
        String resetUrl = AuthUrlUtils.resetPasswordUrl(apiBaseUrl, token);
        String name     = displayName(user);
        int    expiry   = tokenProperties.getResetExpiry();

        emailService.sendEmail(new EmailRequest(
            user.getEmail(),
            name,
            messageSource.getMessage("email.reset.subject", null, locale),
            "password-reset-email",
            Map.of("name", name, "resetUrl", resetUrl, "expiryMinutes", expiry),
            locale
        ));
        log.info("Email de reset de contraseña enviado a '{}'", user.getEmail());
    }

    private String displayName(UserEntity user) {
        return user.getName() != null ? user.getName() : user.getEmail();
    }
}