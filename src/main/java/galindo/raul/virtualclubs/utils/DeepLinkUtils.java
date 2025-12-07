package galindo.raul.virtualclubs.utils;

public class DeepLinkUtils {
  private static final String DEEP_LINK_URL = "virtualclubs://";
  
  private DeepLinkUtils() {}
  
  public static String resetPassword(String token) {
    return String.format("%spass/reset-password?token=%s", DEEP_LINK_URL, token);
  }
  
  public static String verifyEmail(boolean success) {
    return String.format("%semail/verify-email?status=%d", DEEP_LINK_URL, success ? 1 : 0);
  }
}
