package galindo.raul.virtualclubs.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import galindo.raul.virtualclubs.models.entities.AuthProviderEntity;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.exceptions.InvalidTokenException;
import galindo.raul.virtualclubs.repositories.UserEntityRepository;
import galindo.raul.virtualclubs.services.EmailService;
import galindo.raul.virtualclubs.services.GoogleAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para el endpoint POST /v1/auth/google.
 *
 * Verifica los 4 casos de negocio de {@code UserEntityServiceImpl.registerOrLoadUserWithGoogle}:
 * <ol>
 *   <li>Usuario nuevo → cuenta GOOGLE creada, tokens devueltos</li>
 *   <li>Email ya existe con LOCAL verificado → proveedor GOOGLE vinculado</li>
 *   <li>Email ya existe con LOCAL no verificado → cuenta LOCAL eliminada, nueva cuenta GOOGLE</li>
 *   <li>Usuario GOOGLE ya existente → login directo, tokens devueltos</li>
 * </ol>
 *
 * {@link GoogleAuthService} es un {@code @MockitoBean} para no hacer llamadas reales a Google.
 * La BD es H2 en memoria (ver application-test.properties).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
@Transactional
class GoogleAuthIntegrationTest {

    private static final String BASE        = "/v1/auth";
    private static final String EMAIL       = "google@test.com";
    private static final String GOOGLE_SUB  = "google-sub-123";
    private static final String GOOGLE_NAME = "Google User";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserEntityRepository userRepository;

    @MockitoBean private GoogleAuthService googleAuthService;
    @MockitoBean private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Por defecto, cualquier token es válido y representa al usuario de prueba
        when(googleAuthService.verifyToken(anyString())).thenReturn(buildPayload(EMAIL, GOOGLE_SUB, GOOGLE_NAME));
    }

    // ─────────────────────────────────────────────────────────────
    // Caso 1: usuario nuevo
    // ─────────────────────────────────────────────────────────────

    @Test
    void googleLogin_usuarioNuevo_retorna200ConTokens() throws Exception {
        mockMvc.perform(post(BASE + "/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleBody("any-id-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void googleLogin_usuarioNuevo_creaUsuarioEnBDConProviderGoogle() throws Exception {
        assertThat(userRepository.findByEmail(EMAIL)).isEmpty();

        mockMvc.perform(post(BASE + "/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleBody("any-id-token")))
                .andExpect(status().isOk());

        Optional<UserEntity> saved = userRepository.findByEmail(EMAIL);
        assertThat(saved).isPresent();
        assertThat(saved.get().isEmailVerified()).isTrue();
        assertThat(saved.get().getAuthProviderEntities())
                .extracting(AuthProviderEntity::getProviderName)
                .containsExactly("GOOGLE");
    }

    @Test
    void googleLogin_usuarioNuevo_guardaNombreDeGoogle() throws Exception {
        mockMvc.perform(post(BASE + "/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleBody("any-id-token")))
                .andExpect(status().isOk());

        assertThat(userRepository.findByEmail(EMAIL))
                .isPresent()
                .hasValueSatisfying(u -> assertThat(u.getName()).isEqualTo(GOOGLE_NAME));
    }

    // ─────────────────────────────────────────────────────────────
    // Caso 2: email ya existe con cuenta LOCAL verificada
    // ─────────────────────────────────────────────────────────────

    @Test
    void googleLogin_localVerificado_vinculaProviderGoogleAlUsuarioExistente() throws Exception {
        // Crear usuario LOCAL verificado directamente en BD
        UserEntity localUser = crearUsuarioLocalVerificado(EMAIL);

        mockMvc.perform(post(BASE + "/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleBody("any-id-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // El mismo usuario debe tener ahora LOCAL + GOOGLE (no se creó uno nuevo)
        Optional<UserEntity> updated = userRepository.findByEmail(EMAIL);
        assertThat(updated).isPresent();
        assertThat(updated.get().getId()).isEqualTo(localUser.getId());
        assertThat(updated.get().getAuthProviderEntities())
                .extracting(AuthProviderEntity::getProviderName)
                .containsExactlyInAnyOrder("LOCAL", "GOOGLE");
    }

    // ─────────────────────────────────────────────────────────────
    // Caso 3: email ya existe con cuenta LOCAL no verificada
    // ─────────────────────────────────────────────────────────────

    @Test
    void googleLogin_localNoVerificado_eliminaLocalYCreaGoogleUser() throws Exception {
        // Crear usuario LOCAL sin verificar
        UserEntity localUser = crearUsuarioLocalNoVerificado(EMAIL);
        Long oldId = localUser.getId();

        mockMvc.perform(post(BASE + "/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleBody("any-id-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // El usuario viejo debe haber sido eliminado
        assertThat(userRepository.findById(oldId)).isEmpty();

        // Debe existir un nuevo usuario GOOGLE con el mismo email
        Optional<UserEntity> newUser = userRepository.findByEmail(EMAIL);
        assertThat(newUser).isPresent();
        assertThat(newUser.get().getId()).isNotEqualTo(oldId);
        assertThat(newUser.get().isEmailVerified()).isTrue();
        assertThat(newUser.get().getAuthProviderEntities())
                .extracting(AuthProviderEntity::getProviderName)
                .containsExactly("GOOGLE");
    }

    // ─────────────────────────────────────────────────────────────
    // Caso 4: usuario GOOGLE ya existente (login)
    // ─────────────────────────────────────────────────────────────

    @Test
    void googleLogin_usuarioGoogleExistente_loginSinDuplicar() throws Exception {
        // Primer login → crea el usuario
        mockMvc.perform(post(BASE + "/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleBody("token-1")))
                .andExpect(status().isOk());

        long countAfterFirst = userRepository.count();

        // Segundo login con mismo googleId → login directo, no crea duplicado
        mockMvc.perform(post(BASE + "/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleBody("token-2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(userRepository.count()).isEqualTo(countAfterFirst);
    }

    @Test
    void googleLogin_usuarioGoogleExistente_retornaTokensValidos() throws Exception {
        // Primer login
        mockMvc.perform(post(BASE + "/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleBody("token-1")))
                .andExpect(status().isOk());

        // Segundo login → también devuelve tokens
        mockMvc.perform(post(BASE + "/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleBody("token-2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    // ─────────────────────────────────────────────────────────────
    // Validación de entrada
    // ─────────────────────────────────────────────────────────────

    @Test
    void googleLogin_idTokenBlanco_retorna400() throws Exception {
        mockMvc.perform(post(BASE + "/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void googleLogin_cuerpoVacio_retorna400() throws Exception {
        mockMvc.perform(post(BASE + "/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─────────────────────────────────────────────────────────────
    // Token de Google inválido
    // ─────────────────────────────────────────────────────────────

    @Test
    void googleLogin_tokenInvalido_retorna403ConErrorType4() throws Exception {
        when(googleAuthService.verifyToken("bad-token")).thenThrow(new InvalidTokenException());

        mockMvc.perform(post(BASE + "/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleBody("bad-token")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(4)); // ErrorType.INVALID_TOKEN = código 4
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private String googleBody(String idToken) {
        return "{\"idToken\": \"" + idToken + "\"}";
    }

    private GoogleIdToken.Payload buildPayload(String email, String sub, String name) {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail(email);
        payload.setSubject(sub);
        payload.set("name", name);
        return payload;
    }

    /**
     * Crea y persiste un usuario LOCAL con email verificado para simular el caso 2.
     */
    private UserEntity crearUsuarioLocalVerificado(String email) {
        AuthProviderEntity localProvider = AuthProviderEntity.builder()
                .providerName("LOCAL")
                .passwordHash("hashed-password")
                .build();

        UserEntity user = UserEntity.builder()
                .email(email)
                .emailVerified(true)
                .build();
        user.addAuthProvider(localProvider);
        return userRepository.saveAndFlush(user);
    }

    /**
     * Crea y persiste un usuario LOCAL con email sin verificar para simular el caso 3.
     */
    private UserEntity crearUsuarioLocalNoVerificado(String email) {
        AuthProviderEntity localProvider = AuthProviderEntity.builder()
                .providerName("LOCAL")
                .passwordHash("hashed-password")
                .build();

        UserEntity user = UserEntity.builder()
                .email(email)
                .emailVerified(false)
                .build();
        user.addAuthProvider(localProvider);
        return userRepository.saveAndFlush(user);
    }
}
