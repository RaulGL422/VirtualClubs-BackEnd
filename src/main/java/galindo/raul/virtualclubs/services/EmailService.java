package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.config.EmailProperties;
import galindo.raul.virtualclubs.dtos.request.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;
  private final EmailProperties emailProps;
  private final Environment environment;
  
  @Async
  public void sendEmail(EmailRequest request) {
    if (Arrays.asList(environment.getActiveProfiles()).contains("sandbox")) {
      logSandboxEmail(request);
      return;
    }
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      
      helper.setFrom(
          String.format("%s <%s>", emailProps.getFromName(), emailProps.getFrom())
      );
      helper.setTo(request.to());
      helper.setSubject(request.subject());

      // Renderizar plantilla Thymeleaf con el locale del request (resuelve #{...})
      Context context = new Context(request.locale());
      if (request.model() instanceof Map<?, ?> map) {
        map.forEach((k, v) -> context.setVariable(k.toString(), v));
      }

      String html = templateEngine.process(request.template(), context);
      helper.setText(html, true);

      log.debug("[EMAIL] to={} subject='{}' template={} model={}", request.to(), request.subject(), request.template(), request.model());
      mailSender.send(message);
      log.info("Email enviado a: {}", request.to());

    } catch (Exception e) {
      // En @Async void, las excepciones no llegan al llamador — solo se loguean aquí
      // y se propagan al AsyncUncaughtExceptionHandler configurado en AsyncConfig
      log.error("Error enviando email a {}: {}", request.to(), e.getMessage(), e);
    }
  }

  private void logSandboxEmail(EmailRequest request) {
    Object actionUrl = null;
    if (request.model() instanceof Map<?, ?> model) {
      Object verifyUrl = model.get("verifyUrl");
      actionUrl = verifyUrl != null ? verifyUrl : model.get("resetUrl");
    }
    log.info("""

            ╔── SANDBOX — EMAIL INTERCEPTADO ────────────────────────╗
            │  Para:    {}
            │  Asunto:  {}
            │  URL:     {}
            ╚────────────────────────────────────────────────────────╝
            """, request.to(), request.subject(),
            actionUrl != null ? actionUrl : "(sin URL — ver modelo: " + request.model() + ")");
  }
}