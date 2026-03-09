package galindo.raul.virtualclubs.utils;

import galindo.raul.virtualclubs.models.Dispositive;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Utility class for common operations across the application.
 */
public final class CommonUtils {
  /**
   * Extracts device information from an HTTP request.
   *
   * @param request the HttpServletRequest containing device headers
   * @return a Dispositive object populated with device details
   */
  public static Dispositive getDispositiveInfo(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isBlank()) {
      ip = request.getRemoteAddr();
    }
    
    String userAgent = request.getHeader("User-Agent");
    if (userAgent == null) userAgent = "unknown";
    
    String clientDeviceUuid = request.getHeader("X-Device-ID");
    if (clientDeviceUuid == null || clientDeviceUuid.isBlank()) {
      clientDeviceUuid = DigestUtils.sha256Hex(userAgent + ip);
    }
    
    String deviceName = request.getHeader("X-Device-Name");
    String deviceType = request.getHeader("X-Device-Type");
    String deviceId = DigestUtils.sha256Hex(userAgent + ip + clientDeviceUuid);
    
    return new Dispositive(deviceId, deviceName, deviceType, ip);
  }
}
