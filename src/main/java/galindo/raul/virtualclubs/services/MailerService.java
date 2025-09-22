package galindo.raul.virtualclubs.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class MailerService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendHtmlEmail(String to, String subject, String body) {
        try {
            // Crear el mensaje MIME para correos HTML
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);

            helper.setText(body, true);

            // Enviar el correo
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String generatePasswordResetEmail(String name, String token) {
        String resetUrl = String.format("%s/api/auth/reset-password?token=%s", baseUrl, token);

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("resetUrl", resetUrl);

        // Procesar la plantilla y devolver el contenido generado
        return templateEngine.process("password-reset-email", context);
    }
}
