package galindo.raul.virtualclubs.services;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import galindo.raul.virtualclubs.models.exceptions.MailSendException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MailerService {

    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Value("${app.mail.sendGridApiKey}")
    private String sendGridApiKey;

    @Value("${app.deeplink-url}")
    private String baseUrl;

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        Email from = new Email(fromEmail);
        Email recipient = new Email(to);
        Content content = new Content("text/html", htmlBody);
        Mail mail = new Mail(from, subject, recipient, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() >= 400) {
                throw new MailSendException(response.getBody());
            }
        } catch (IOException e) {
            throw new MailSendException(e.getMessage());
        }
    }

    // --- Generate HTML content from Thymeleaf template ---
    public String generatePasswordResetEmail(String name, String token) {
        String resetUrl = String.format("%s/api/auth/reset-password-redirect?token=%s", baseUrl, token);

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("resetUrl", resetUrl);

        return templateEngine.process("password-reset-email", context);
    }

    public String generateVerifyEmail(String name, String token) {
        String verifyUrl = String.format("%s/api/auth/verify?token=%s", baseUrl, token);

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("verifyUrl", verifyUrl);

        return templateEngine.process("verify-email", context);
    }
}
