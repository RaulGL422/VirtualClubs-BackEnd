package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.config.TokenProperties;
import galindo.raul.virtualclubs.dtos.request.EmailRequest;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.enums.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para NotificationService.
 * Sin contexto Spring — Mockito puro.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private UserTokenServiceImpl userTokenService;
    @Mock private EmailService emailService;
    @Mock private TokenProperties tokenProperties;
    @Mock private MessageSource messageSource;

    @InjectMocks private NotificationService notificationService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder().id(1L).email("notify@test.com").name("Carlos").build();
        ReflectionTestUtils.setField(notificationService, "apiBaseUrl", "https://api-vc.rgal.dev");
    }

    // ─────────────────────────────────────────────────────────────
    // sendVerificationEmail
    // ─────────────────────────────────────────────────────────────

    @Test
    void sendVerificationEmail_creaTokenYEnviaEmailAlDestinatario() {
        when(userTokenService.createTokenFor(user, TokenType.EMAIL_VERIFICATION)).thenReturn("plain-token");
        when(tokenProperties.getVerificationExpiry()).thenReturn(1440);
        when(messageSource.getMessage(eq("email.verify.subject"), any(), any(Locale.class)))
                .thenReturn("Verifica tu email");

        notificationService.sendVerificationEmail(user, Locale.forLanguageTag("es"));

        ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailService).sendEmail(captor.capture());

        EmailRequest sent = captor.getValue();
        assertThat(sent.to()).isEqualTo("notify@test.com");
        assertThat(sent.toName()).isEqualTo("Carlos");
        assertThat(sent.subject()).isEqualTo("Verifica tu email");
        assertThat(sent.template()).isEqualTo("verify-email");
        assertThat(sent.locale()).isEqualTo(Locale.forLanguageTag("es"));
    }

    @Test
    void sendVerificationEmail_urlContieneToken() {
        when(userTokenService.createTokenFor(user, TokenType.EMAIL_VERIFICATION)).thenReturn("abc123");
        when(tokenProperties.getVerificationExpiry()).thenReturn(1440);
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("subject");

        notificationService.sendVerificationEmail(user, Locale.ENGLISH);

        ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailService).sendEmail(captor.capture());

        @SuppressWarnings("unchecked")
        Map<String, Object> model = (Map<String, Object>) captor.getValue().model();
        assertThat(model.get("verifyUrl").toString())
                .contains("/v1/auth/verify?token=")
                .contains("abc123");
    }

    @Test
    void sendVerificationEmail_expiryEnHoras() {
        when(userTokenService.createTokenFor(any(), any())).thenReturn("token");
        when(tokenProperties.getVerificationExpiry()).thenReturn(1440); // 24 horas
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("subject");

        notificationService.sendVerificationEmail(user, Locale.forLanguageTag("es"));

        ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailService).sendEmail(captor.capture());

        @SuppressWarnings("unchecked")
        Map<String, Object> model = (Map<String, Object>) captor.getValue().model();
        assertThat(model.get("expiryHours")).isEqualTo(24); // 1440 / 60
    }

    @Test
    void sendVerificationEmail_usaLocaleEnElRequest() {
        when(userTokenService.createTokenFor(any(), any())).thenReturn("token");
        when(tokenProperties.getVerificationExpiry()).thenReturn(60);
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("subject");

        notificationService.sendVerificationEmail(user, Locale.ENGLISH);

        ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailService).sendEmail(captor.capture());
        assertThat(captor.getValue().locale()).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void sendVerificationEmail_usaEmailComoNombreSiNameEsNull() {
        UserEntity userSinNombre = UserEntity.builder().id(2L).email("anon@test.com").build();
        when(userTokenService.createTokenFor(any(), any())).thenReturn("token");
        when(tokenProperties.getVerificationExpiry()).thenReturn(60);
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("subject");

        notificationService.sendVerificationEmail(userSinNombre, Locale.forLanguageTag("es"));

        ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailService).sendEmail(captor.capture());
        assertThat(captor.getValue().toName()).isEqualTo("anon@test.com");
    }

    @Test
    void sendVerificationEmail_fallaCreacionDeToken_nuncaEnviaEmail() {
        when(userTokenService.createTokenFor(user, TokenType.EMAIL_VERIFICATION))
                .thenThrow(new RuntimeException("error de base de datos"));

        assertThatThrownBy(() -> notificationService.sendVerificationEmail(user, Locale.forLanguageTag("es")))
                .isInstanceOf(RuntimeException.class);

        verifyNoInteractions(emailService);
    }

    // ─────────────────────────────────────────────────────────────
    // sendPasswordResetEmail
    // ─────────────────────────────────────────────────────────────

    @Test
    void sendPasswordResetEmail_creaTokenYEnviaEmailConTemplate() {
        when(userTokenService.createTokenFor(user, TokenType.PASSWORD_RESET)).thenReturn("reset-token");
        when(tokenProperties.getResetExpiry()).thenReturn(30);
        when(messageSource.getMessage(eq("email.reset.subject"), any(), any(Locale.class)))
                .thenReturn("Restablecer contraseña");

        notificationService.sendPasswordResetEmail(user, Locale.forLanguageTag("es"));

        ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailService).sendEmail(captor.capture());

        EmailRequest sent = captor.getValue();
        assertThat(sent.to()).isEqualTo("notify@test.com");
        assertThat(sent.template()).isEqualTo("password-reset-email");
        assertThat(sent.subject()).isEqualTo("Restablecer contraseña");
    }

    @Test
    void sendPasswordResetEmail_urlContieneTokenYRedirect() {
        when(userTokenService.createTokenFor(any(), any())).thenReturn("reset-abc");
        when(tokenProperties.getResetExpiry()).thenReturn(30);
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("subject");

        notificationService.sendPasswordResetEmail(user, Locale.ENGLISH);

        ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailService).sendEmail(captor.capture());

        @SuppressWarnings("unchecked")
        Map<String, Object> model = (Map<String, Object>) captor.getValue().model();
        assertThat(model.get("resetUrl").toString())
                .contains("/v1/auth/resetPasswordRedirect?token=")
                .contains("reset-abc");
    }

    @Test
    void sendPasswordResetEmail_expiryEnMinutos() {
        when(userTokenService.createTokenFor(any(), any())).thenReturn("token");
        when(tokenProperties.getResetExpiry()).thenReturn(30);
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("subject");

        notificationService.sendPasswordResetEmail(user, Locale.forLanguageTag("es"));

        ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailService).sendEmail(captor.capture());

        @SuppressWarnings("unchecked")
        Map<String, Object> model = (Map<String, Object>) captor.getValue().model();
        assertThat(model.get("expiryMinutes")).isEqualTo(30);
    }
}