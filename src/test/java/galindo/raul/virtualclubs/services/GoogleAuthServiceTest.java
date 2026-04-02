package galindo.raul.virtualclubs.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import galindo.raul.virtualclubs.models.exceptions.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para GoogleAuthService.verifyToken().
 *
 * Estrategia: construir el servicio con un clientId de prueba (el constructor no hace
 * llamadas externas) y reemplazar el {@link GoogleIdTokenVerifier} interno por un mock
 * con {@link ReflectionTestUtils} para controlar los distintos escenarios de verificación.
 */
@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceTest {

    private GoogleAuthService service;
    private GoogleIdTokenVerifier mockVerifier;

    private static final String VALID_TOKEN = "valid-id-token";
    private static final String EMAIL       = "google@test.com";
    private static final String GOOGLE_SUB  = "google-sub-123";
    private static final String GOOGLE_NAME = "Test User";

    @BeforeEach
    void setUp() {
        // new NetHttpTransport() y GsonFactory no hacen llamadas de red en el constructor
        service = new GoogleAuthService("fake-client-id");
        mockVerifier = mock(GoogleIdTokenVerifier.class);
        ReflectionTestUtils.setField(service, "verifier", mockVerifier);
    }

    // ─────────────────────────────────────────────────────────────
    // verifyToken — caso feliz
    // ─────────────────────────────────────────────────────────────

    @Test
    void verifyToken_tokenValido_retornaPayloadConEmailSubYName() throws Exception {
        GoogleIdToken.Payload payload = buildPayload(EMAIL, GOOGLE_SUB, GOOGLE_NAME);
        GoogleIdToken mockToken = mock(GoogleIdToken.class);
        when(mockToken.getPayload()).thenReturn(payload);
        when(mockVerifier.verify(VALID_TOKEN)).thenReturn(mockToken);

        GoogleIdToken.Payload result = service.verifyToken(VALID_TOKEN);

        assertThat(result.getEmail()).isEqualTo(EMAIL);
        assertThat(result.getSubject()).isEqualTo(GOOGLE_SUB);
        assertThat(result.get("name")).isEqualTo(GOOGLE_NAME);
    }

    @Test
    void verifyToken_tokenValidoSinName_retornaPayloadConNameNull() throws Exception {
        GoogleIdToken.Payload payload = buildPayload(EMAIL, GOOGLE_SUB, null);
        GoogleIdToken mockToken = mock(GoogleIdToken.class);
        when(mockToken.getPayload()).thenReturn(payload);
        when(mockVerifier.verify(VALID_TOKEN)).thenReturn(mockToken);

        GoogleIdToken.Payload result = service.verifyToken(VALID_TOKEN);

        assertThat(result.getEmail()).isEqualTo(EMAIL);
        assertThat(result.get("name")).isNull();
    }

    // ─────────────────────────────────────────────────────────────
    // verifyToken — token inválido o expirado
    // ─────────────────────────────────────────────────────────────

    @Test
    void verifyToken_verifierRetornaNull_lanzaInvalidTokenException() throws Exception {
        when(mockVerifier.verify(anyString())).thenReturn(null);

        assertThatThrownBy(() -> service.verifyToken("expired-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void verifyToken_generalSecurityException_lanzaInvalidTokenException() throws Exception {
        when(mockVerifier.verify(anyString())).thenThrow(new GeneralSecurityException("firma inválida"));

        assertThatThrownBy(() -> service.verifyToken("tampered-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void verifyToken_ioException_lanzaInvalidTokenException() throws Exception {
        when(mockVerifier.verify(anyString())).thenThrow(new IOException("network error"));

        assertThatThrownBy(() -> service.verifyToken("network-error-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    // ─────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────

    private GoogleIdToken.Payload buildPayload(String email, String sub, String name) {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail(email);
        payload.setSubject(sub);
        if (name != null) {
            payload.set("name", name);
        }
        return payload;
    }
}
