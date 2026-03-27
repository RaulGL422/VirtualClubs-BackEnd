package galindo.raul.virtualclubs.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the various error types and their corresponding numeric codes within the application.
 */
public enum ErrorType {
  INTERNAL_ERROR(1),
  INVALID_CREDENTIALS(2),
  USERNAME_NOT_FOUND(3),
  EMAIL_ALREADY_EXISTS(4),
  EMAIL_REQUIRED(5),
  PASSWORD_REQUIRED(6),
  USERNAME_REQUIRED(7),
  PASSWORD_MIN_LENGTH_ERROR(8),
  INVALID_EMAIL_FORMAT(9),
  FIELD_NULL(10),
  TOKEN_BLANK(11),
  INVALID_REFRESH_TOKEN(12),
  INVALID_GOOGLE_TOKEN(13),
  FAILED_SEND_EMAIL(14),
  INVALID_TOKEN(15),
  NO_LOCAL_PROVIDER(16),
  EMAIL_NOT_FOUND(17),
  INVALID_ACCESS_TOKEN(18),
  CANT_CONNECT_SERVER(19),
  MISSING_TOKENS(20),
  GOOGLE_SIGN_IN_FAILED(21),
  PASSWORD_NOT_EQUALS(22),
  GOOGLE_SIGN_IN_NO_TOKEN(23),
  GOOGLE_LOGIN_EXCEPTION(24),
  USER_NOT_FOUND(25),
  EMAIL_NOT_VERIFIED(26),
  PASSWORD_NOT_STRONG(27),
  RATE_LIMIT_EXCEEDED(28);
  
  private final int code;
  
  /**
   * Constructor for ErrorType.
   *
   * @param code The integer code associated with the error.
   */
  ErrorType(int code) {
    this.code = code;
  }
  
  /**
   * Gets the integer code of the error type.
   * This value is used for JSON serialization.
   *
   * @return The error code.
   */
  @JsonValue
  public int getCode() {
    return code;
  }
  
  /**
   * Maps an integer code to its corresponding ErrorType.
   *
   * @param code The integer code to look up.
   * @return The matching ErrorType.
   * @throws IllegalArgumentException if the code does not match any ErrorType.
   */
  public static ErrorType fromIntCode(int code) {
    for (ErrorType errorType : values()) {
      if (errorType.getCode() == code) {
        return errorType;
      }
    }
    throw new IllegalArgumentException(String.format("Invalid error code: %s", code));
  }
  
  /**
   * Maps a string representation of an integer code to its corresponding ErrorType.
   *
   * @param code The string code to parse and look up.
   * @return The matching ErrorType, or INTERNAL_ERROR if the string is not a valid integer.
   */
  public static ErrorType fromStringCode(String code) {
    try {
      return fromIntCode(Integer.parseInt(code));
    } catch (NumberFormatException e) {
      return ErrorType.INTERNAL_ERROR;
    }
  }
}
