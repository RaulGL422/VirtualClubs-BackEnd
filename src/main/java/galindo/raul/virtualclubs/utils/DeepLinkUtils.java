package galindo.raul.virtualclubs.utils;

/**
 * Genera deep links {@code virtualclubs://} para redirigir a la app móvil
 * después de que el servidor procese un token de autenticación.
 *
 * <p>A diferencia de {@link AuthUrlUtils} (que genera URLs HTTP hacia la API),
 * esta clase genera URIs de esquema personalizado que la app Android intercepta.
 */
public final class DeepLinkUtils {
  private static final String DEEP_LINK_URL = "virtualclubs://";

  private DeepLinkUtils() {}

  /**
   * Deep link al flujo de reset de contraseña con el token incluido.
   *
   * @param token token de reset codificado en URL (ya debe venir codificado)
   * @return URI del tipo {@code virtualclubs://pass/resetPassword?token=...}
   */
  public static String resetPassword(String token) {
    return String.format("%spass/resetPassword?token=%s", DEEP_LINK_URL, token);
  }

  /**
   * Deep link al flujo de verificación de email con el resultado.
   *
   * @param success {@code true} si la verificación fue exitosa
   * @return URI del tipo {@code virtualclubs://email/verifyEmail?status=1} o {@code ?status=0}
   */
  public static String verifyEmail(boolean success) {
    return String.format("%semail/verifyEmail?status=%d", DEEP_LINK_URL, success ? 1 : 0);
  }
}
