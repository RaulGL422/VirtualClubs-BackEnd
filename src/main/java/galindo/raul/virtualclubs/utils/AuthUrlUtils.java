package galindo.raul.virtualclubs.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Centraliza la construcción de URLs de la API que se incluyen en los emails de autenticación.
 *
 * <p>A diferencia de {@link DeepLinkUtils} (que genera deep links {@code virtualclubs://} para la app móvil),
 * esta clase genera URLs HTTP hacia los endpoints del servidor que procesan los tokens de un solo uso.
 */
public class AuthUrlUtils {

  private AuthUrlUtils() {}

  /**
   * URL del endpoint que verifica el email del usuario.
   *
   * @param apiBaseUrl base de la API (ej. {@code https://api-vc.rgal.dev})
   * @param token      token de verificación en texto plano
   * @return URL completa lista para incrustar en el email
   */
  public static String verifyEmailUrl(String apiBaseUrl, String token) {
    return apiBaseUrl + "/v1/auth/verify?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
  }

  /**
   * URL del endpoint que redirige al deep link de reset de contraseña.
   *
   * @param apiBaseUrl base de la API
   * @param token      token de reset en texto plano
   * @return URL completa lista para incrustar en el email
   */
  public static String resetPasswordUrl(String apiBaseUrl, String token) {
    return apiBaseUrl + "/v1/auth/resetPasswordRedirect?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
  }
}
