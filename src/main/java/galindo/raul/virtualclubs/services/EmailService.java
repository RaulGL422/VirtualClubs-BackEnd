package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.config.EmailProperties;
import galindo.raul.virtualclubs.dtos.request.EmailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {
  
  private static final Logger log = LoggerFactory.getLogger(EmailService.class);
  
  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;
  private final EmailProperties emailProps;
  
  @Async
  public void sendEmail(EmailRequest request) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      
      helper.setFrom(
          String.format("%s <%s>", emailProps.getFromName(), emailProps.getFrom())
      );
      helper.setTo(request.to());
      helper.setSubject(request.subject());

      // Renderizar plantilla Thymeleaf
      Context context = new Context();
      if (request.model() instanceof Map<?, ?> map) {
        map.forEach((k, v) -> context.setVariable(k.toString(), v));
      }

      String html = templateEngine.process(request.template(), context);
      helper.setText(html, true);

      mailSender.send(message);
      log.info("Email enviado a: {}", request.to());

    } catch (Exception e) {
      // En @Async void, las excepciones no llegan al llamador — solo se loguean aquí
      // y se propagan al AsyncUncaughtExceptionHandler configurado en AsyncConfig
      log.error("Error enviando email a {}: {}", request.to(), e.getMessage(), e);
    }
  }
}