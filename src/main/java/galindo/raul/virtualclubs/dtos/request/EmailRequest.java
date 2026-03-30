package galindo.raul.virtualclubs.dtos.request;

import java.util.Locale;

/**
 * Solicitud de envío de email.
 *
 * @param to       Destinatario (dirección de email)
 * @param toName   Nombre del destinatario (para personalización)
 * @param subject  Asunto del email (ya traducido)
 * @param template Nombre de la plantilla Thymeleaf (sin extensión)
 * @param model    Variables dinámicas accesibles en la plantilla con ${...}
 * @param locale   Idioma para resolver #{...} en la plantilla
 */
public record EmailRequest(String to, String toName, String subject, String template, Object model, Locale locale) {

    /** Constructor de compatibilidad — usa español como idioma por defecto. */
    public EmailRequest(String to, String toName, String subject, String template, Object model) {
        this(to, toName, subject, template, model, Locale.forLanguageTag("es"));
    }
}