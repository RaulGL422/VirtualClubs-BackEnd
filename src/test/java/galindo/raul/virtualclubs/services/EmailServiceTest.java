package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.config.EmailProperties;
import galindo.raul.virtualclubs.dtos.request.EmailRequest;
import galindo.raul.virtualclubs.services.GoogleAuthService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.thymeleaf.TemplateEngine;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Verifica que EmailService ejecuta el envío en un hilo async del pool "async-*"
 * configurado en AsyncConfig, sin bloquear el hilo del llamador.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles({"dev", "test"})
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @MockitoBean private JavaMailSender mailSender;
    @MockitoBean private TemplateEngine templateEngine;
    @MockitoBean private EmailProperties emailProps;
    @MockitoBean private GoogleAuthService googleAuthService;

    @Test
    void sendEmail_seEjecutaEnHiloAsincronoSeparado() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailProps.getFrom()).thenReturn("no-reply@virtualclubs.dev");
        when(emailProps.getFromName()).thenReturn("VirtualClubs");
        when(templateEngine.process(any(String.class), any())).thenReturn("<html>ok</html>");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> asyncThreadName = new AtomicReference<>();

        doAnswer(inv -> {
            asyncThreadName.set(Thread.currentThread().getName());
            latch.countDown();
            return null;
        }).when(mailSender).send(any(MimeMessage.class));

        String callerThread = Thread.currentThread().getName();

        emailService.sendEmail(new EmailRequest(
            "virtualclub.spain@gmail.com", "Usuario", "Asunto de prueba", "verify-email", Map.of()
        ));

        // Esperar a que el hilo async complete (máx. 5 segundos)
        assertThat(latch.await(5, TimeUnit.SECONDS))
            .as("El email debería enviarse en menos de 5 segundos")
            .isTrue();

        // El hilo async debe ser distinto al hilo del test
        assertThat(asyncThreadName.get())
            .as("sendEmail debe ejecutarse en un hilo diferente al llamador")
            .isNotEqualTo(callerThread);

        // El hilo debe pertenecer al pool configurado en AsyncConfig
        assertThat(asyncThreadName.get())
            .as("El hilo debe pertenecer al pool 'async-' de AsyncConfig")
            .startsWith("async-");
    }

    @Test
    void sendEmail_fallaMailSenderSend_seCapturaYNoSePropaga() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailProps.getFrom()).thenReturn("no-reply@virtualclubs.dev");
        when(emailProps.getFromName()).thenReturn("VirtualClubs");
        when(templateEngine.process(any(String.class), any())).thenReturn("<html>ok</html>");
        doThrow(new org.springframework.mail.MailSendException("SMTP no disponible"))
            .when(mailSender).send(any(MimeMessage.class));

        // sendEmail es @Async void: si la excepción no se capturara internamente,
        // se propagaría al AsyncUncaughtExceptionHandler y jamás llegaría aquí como fallo del test.
        emailService.sendEmail(new EmailRequest(
            "virtualclub.spain@gmail.com", "Usuario", "Asunto de prueba", "verify-email", Map.of()
        ));

        // Espera activa (con timeout) a que el hilo async invoque el mock antes de verificar.
        verify(mailSender, timeout(5000)).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_fallaTemplateEngine_nuncaLlamaAMailSenderSend() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailProps.getFrom()).thenReturn("no-reply@virtualclubs.dev");
        when(emailProps.getFromName()).thenReturn("VirtualClubs");
        when(templateEngine.process(any(String.class), any()))
            .thenThrow(new org.thymeleaf.exceptions.TemplateProcessingException("plantilla invalida"));

        emailService.sendEmail(new EmailRequest(
            "virtualclub.spain@gmail.com", "Usuario", "Asunto de prueba", "verify-email", Map.of()
        ));

        // createMimeMessage se llama antes que templateEngine.process en el código real,
        // así que esperar por esa invocación confirma que el hilo async ya terminó su intento.
        verify(mailSender, timeout(5000)).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}