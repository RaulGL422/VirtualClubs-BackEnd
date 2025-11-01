package galindo.raul.virtualclubs.utils;

public class DeepLinkUtils {
  private static String deepLinkUrl = "virtualclubs://";
  
  private DeepLinkUtils() {}
  
  public static String resetPassword(String token) {
    return String.format("%spass/reset-password?token=%s", deepLinkUrl, token);
  }
  
  public static String verifyEmail(boolean success) {
    return String.format("%semail/verify-email?status=%d", deepLinkUrl, success ? 1 : 0);
  }
}
