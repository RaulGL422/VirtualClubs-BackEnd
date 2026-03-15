package galindo.raul.virtualclubs.dtos.response;

import galindo.raul.virtualclubs.models.enums.ErrorType;

/**
 * A generic wrapper for API responses.
 *
 * @param <T>     the type of the data payload
 * @param success indicates if the operation was successful
 * @param message the error type if the operation failed, otherwise null
 * @param data    the data payload if the operation was successful
 */
public record ApiResponse<T>(boolean success, ErrorType message, T data) {
  /**
   * Creates a successful API response.
   *
   * @param data the data to include in the response
   * @return a new ApiResponse instance indicating success
   */
  public static <T> ApiResponse<T> success(T data) { return new ApiResponse<>(true, null, data); }
  
  /**
   * Creates a successful empty API response.
   *
   * @return a new ApiResponse instance indicating success
   */
  public static <T> ApiResponse<Void> emptySuccess() { return new ApiResponse<Void>(true, null, null); }
  
  /**
   * Creates an error API response.
   *
   * @param message the error type describing the failure
   * @return a new ApiResponse instance indicating failure
   */
  public static <T> ApiResponse<T> error(ErrorType message) { return new ApiResponse<>(false, message, null); }
}
