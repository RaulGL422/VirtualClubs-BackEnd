package galindo.raul.virtualclubs.controller;

import galindo.raul.virtualclubs.dtos.response.ApiResponse;
import galindo.raul.virtualclubs.models.enums.ErrorType;
import galindo.raul.virtualclubs.models.exceptions.*;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * GlobalExceptionHandler provides centralized exception handling for the VirtualClubs-BackEnd application.
 * It intercepts various types of exceptions thrown by controllers and services,
 * logs them, and returns a consistent ApiResponse structure to the client.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  /**
   * Handles validation errors thrown by {@code @Valid} annotations on method arguments.
   * It extracts error messages, attempts to parse an error code from the first message,
   * and returns a bad request response with a specific error type.
   *
   * @param ex The {@link MethodArgumentNotValidException} that occurred.
   * @return A {@link ResponseEntity} containing an {@link ApiResponse} with validation error details.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    List<String> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .toList();
    return ResponseEntity.badRequest()
        .body(ApiResponse.error(resolveErrorType(errors.getFirst())));
  }
  
  /**
   * Handles exceptions that occur when the request body cannot be read or parsed,
   * typically due to malformed JSON or missing required fields.
   *
   * @param ex The {@link HttpMessageNotReadableException} that occurred.
   * @return A {@link ResponseEntity} containing an {@link ApiResponse} indicating a field null error.
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
    log.warn("⚠️ Invalid or missing fields in request body: {}", ex.getMessage());
    return ResponseEntity.badRequest()
        .body(ApiResponse.error(ErrorType.FIELD_NULL));
  }
  
  /**
   * Handles {@link UsernameNotFoundException} when a requested user cannot be found.
   *
   * @param e The {@link UsernameNotFoundException} that occurred.
   * @return A {@link ResponseEntity} with HTTP status 404 (NOT_FOUND) and an {@link ApiResponse} for user not found.
   */
  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleUsernameNotFound(UsernameNotFoundException e) {
    log.warn("⚠️ User not found: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ErrorType.USERNAME_NOT_FOUND));
  }
  
  /**
   * Handles {@link EmailNotFoundException} when a specified email address is not found in the system.
   *
   * @param e The {@link EmailNotFoundException} that occurred.
   * @return A {@link ResponseEntity} with HTTP status 404 (NOT_FOUND) and an {@link ApiResponse} for email not found.
   */
  @ExceptionHandler(EmailNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleEmailNotFound(EmailNotFoundException e) {
    log.warn("⚠️ Email '{}' not found", e.getEmail());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ErrorType.EMAIL_NOT_FOUND));
  }
  
  /**
   * Handles {@link UserAlreadyExistException} when an attempt is made to register a user with an email that already exists.
   *
   * @param e The {@link UserAlreadyExistException} that occurred.
   * @return A {@link ResponseEntity} with HTTP status 409 (CONFLICT) and an {@link ApiResponse} for email already exists.
   */
  @ExceptionHandler(UserAlreadyExistException.class)
  public ResponseEntity<ApiResponse<Void>> handleUserAlreadyExist(UserAlreadyExistException e) {
    log.warn("⚠️ User already exists with email: '{}'", e.getEmail());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiResponse.error(ErrorType.EMAIL_ALREADY_EXISTS));
  }
  
  /**
   * Handles {@link UserNotFoundException} when a user cannot be found in the system.
   *
   * @param e The {@link UserNotFoundException} that occurred.
   * @return A {@link ResponseEntity} with HTTP status 404 (NOT_FOUND) and an {@link ApiResponse} for user not found.
   */
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException e) {
    log.warn("⚠️ User '{}' not found", e.getEmail());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ErrorType.USER_NOT_FOUND));
  }
  
  /**
   * Handles {@link RefreshTokenException} when a refresh token is invalid or expired.
   *
   * @param e The {@link RefreshTokenException} that occurred.
   * @return A {@link ResponseEntity} with HTTP status 401 (UNAUTHORIZED) and an {@link ApiResponse} for an invalid refresh token.
   */
  @ExceptionHandler(RefreshTokenException.class)
  public ResponseEntity<ApiResponse<Void>> handleRefreshTokenException(RefreshTokenException e) {
    log.warn("⚠️ Invalid or expired refresh token");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error(ErrorType.INVALID_REFRESH_TOKEN));
  }
  
  /**
   * Handles {@link NoLocalProviderException} when a user attempts an action (e.g., password reset)
   * that requires a local provider but the user is registered via an external provider.
   *
   * @param e The {@link NoLocalProviderException} that occurred.
   * @return A {@link ResponseEntity} with HTTP status 400 (BAD_REQUEST) and an {@link ApiResponse} for no local provider.
   */
//  @ExceptionHandler(NoLocalProviderException.class)
//  public ResponseEntity<ApiResponse<Void>> handleNoLocalProvider(NoLocalProviderException e) {
//    log.warn("⚠️ User '{}' requested password reset but has no LOCAL provider", e.getEmail());
//    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//        .body(new ApiResponse<>(false, ErrorType.NO_LOCAL_PROVIDER, ResponseType.ERROR, null));
//  }
  
//  @ExceptionHandler(EmailNotVerifiedException.class)
//  public ResponseEntity<ApiResponse<Void>> handleEmailNotVerified(EmailNotVerifiedException e) {
//    log.warn("⚠️ User '{}' not verified", e.getEmail());
//    return ResponseEntity.status(HttpStatus.FORBIDDEN)
//        .body(new ApiResponse<>(false, ErrorType.EMAIL_NOT_VERIFIED, ResponseType.ERROR, null));
//  }
//
//  /**
//   * Handles {@link GoogleIdException} when a Google ID token is invalid.
//   * @param e The {@link GoogleIdException} that occurred.
//   * @return A {@link ResponseEntity} with HTTP status 403 (FORBIDDEN) and an {@link ApiResponse} for an invalid Google token.
//   */
//  @ExceptionHandler(GoogleIdException.class)
//  public ResponseEntity<ApiResponse<Void>> handleGoogleId(GoogleIdException e) {
//    log.warn("❌ Invalid Google ID token: {}", e.getMessage());
//    return ResponseEntity.status(HttpStatus.FORBIDDEN)
//        .body(new ApiResponse<>(false, ErrorType.INVALID_GOOGLE_TOKEN, ResponseType.ERROR, null));
//  }
//
//  /**
//   * Handles {@link InvalidTokenException} when a generic token (e.g., for password reset or email verification) is invalid.
//   * @param e The {@link InvalidTokenException} that occurred.
//   * @return A {@link ResponseEntity} with HTTP status 403 (FORBIDDEN) and an {@link ApiResponse} for an invalid token.
//   */
//  @ExceptionHandler(InvalidTokenException.class)
//  public ResponseEntity<ApiResponse<Void>> handleInvalidToken(InvalidTokenException e) {
//    log.warn("❌ Invalid token detected");
//    return ResponseEntity.status(HttpStatus.FORBIDDEN)
//        .body(new ApiResponse<>(false, ErrorType.INVALID_TOKEN, ResponseType.ERROR, null));
//  }
//
//  /**
//   * Handles {@link MailSendException} when there is a failure in sending an email.
//   * @param e The {@link MailSendException} that occurred.
//   * @return A {@link ResponseEntity} with HTTP status 500 (INTERNAL_SERVER_ERROR) and an {@link ApiResponse} for failed email sending.
//   */
//  @ExceptionHandler(MailSendException.class)
//  public ResponseEntity<ApiResponse<Void>> handleMailSend(MailSendException e) {
//    log.error("❌ Failed to send mail: {}", e.getMessage());
//    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//        .body(new ApiResponse<>(false, ErrorType.FAILED_SEND_EMAIL, ResponseType.ERROR, null));
//  }

  /**
   * Handles {@link JwtException} when a JWT token is malformed, expired, or otherwise invalid.
   * Returns 401 UNAUTHORIZED so the client knows it must re-authenticate, not that it has no permission.
   *
   * @param e The {@link JwtException} that occurred.
   * @return A {@link ResponseEntity} with HTTP status 401 and an {@link ApiResponse} for invalid refresh token.
   */
  @ExceptionHandler(JwtException.class)
  public ResponseEntity<ApiResponse<Void>> handleJwtException(JwtException e) {
    log.warn("⚠️ Malformed or invalid JWT: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error(ErrorType.INVALID_REFRESH_TOKEN));
  }

  /**
   * Handles {@link InternalErrorException} for custom internal errors within the application.
   * @param e The {@link InternalErrorException} that occurred.
   * @return A {@link ResponseEntity} with HTTP status 500 (INTERNAL_SERVER_ERROR) and an {@link ApiResponse} for an internal error.
   */
  @ExceptionHandler(InternalErrorException.class)
  public ResponseEntity<ApiResponse<Void>> handleInternalError(InternalErrorException e) {
    log.error("❌ Internal error: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(ErrorType.INTERNAL_ERROR));
  }

  /**
   * A catch-all exception handler for any unexpected exceptions not specifically handled by other methods.
   * @param e The generic {@link Exception} that occurred.
   * @return A {@link ResponseEntity} with HTTP status 500 (INTERNAL_SERVER_ERROR) and a generic internal error {@link ApiResponse}.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
    log.error("💥 Unexpected error: {}", e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(ErrorType.INTERNAL_ERROR));
  }

  /** Resuelve el ErrorType a partir del mensaje de validación, que puede ser el nombre del enum
   *  (ej. "EMAIL_REQUIRED") o, por compatibilidad, el código numérico como string (ej. "5"). */
  private ErrorType resolveErrorType(String message) {
    try {
      return ErrorType.valueOf(message);
    } catch (IllegalArgumentException e) {
      return ErrorType.fromStringCode(message);
    }
  }
}