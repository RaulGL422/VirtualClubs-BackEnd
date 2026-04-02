package galindo.raul.virtualclubs.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import galindo.raul.virtualclubs.models.exceptions.InvalidTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Servicio para verificar ID Tokens de Google OAuth2.
 * Utiliza {@link GoogleIdTokenVerifier} para validar la firma y la audiencia del token
 * contra el Client ID configurado.
 */
@Slf4j
@Service
public class GoogleAuthService {

    private final GoogleIdTokenVerifier verifier;

    @Autowired
    public GoogleAuthService(@Value("${google.id}") String clientId) {
        verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    /**
     * Verifica un ID Token de Google y devuelve su payload si es válido.
     * Encapsula cualquier error de verificación en {@link InvalidTokenException}
     * para que {@code GlobalExceptionHandler} lo gestione de forma centralizada.
     *
     * @param idTokenString el ID Token JWT recibido del cliente Android
     * @return el {@link GoogleIdToken.Payload} con los datos del usuario (email, sub, name, etc.)
     * @throws InvalidTokenException si el token es inválido, ha expirado o no corresponde al Client ID
     */
    public GoogleIdToken.Payload verifyToken(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload();
            }
            log.warn("Google ID token verification returned null — token inválido o expirado");
            throw new InvalidTokenException();
        } catch (GeneralSecurityException | IOException e) {
            log.warn("Error al verificar Google ID token: {}", e.getMessage());
            throw new InvalidTokenException();
        }
    }
}
