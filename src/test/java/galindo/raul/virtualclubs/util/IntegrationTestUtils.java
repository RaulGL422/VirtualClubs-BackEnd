package galindo.raul.virtualclubs.util;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Helpers reutilizables para tests de integración de AuthController.
 */
public final class IntegrationTestUtils {

    private IntegrationTestUtils() {}

    /**
     * Construye el body JSON de login/registro con email y contraseña.
     */
    public static String authBody(String email, String password) {
        return "{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}";
    }

    /**
     * Registra un usuario vía POST /v1/auth/register.
     */
    public static void register(MockMvc mockMvc, String email, String password) throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authBody(email, password)));
    }

    /**
     * Hace login y devuelve el access token del response.
     */
    public static String loginAndGetAccessToken(MockMvc mockMvc, ObjectMapper objectMapper,
                                                String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authBody(email, password)))
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("data").get("accessToken").asText();
    }

    /**
     * Hace login y devuelve el refresh token del response.
     */
    public static String loginAndGetRefreshToken(MockMvc mockMvc, ObjectMapper objectMapper,
                                                 String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authBody(email, password)))
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("data").get("refreshToken").asText();
    }
}
