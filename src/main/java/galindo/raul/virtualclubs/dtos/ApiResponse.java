package galindo.raul.virtualclubs.dtos;

import galindo.raul.virtualclubs.models.enums.ErrorType;
import galindo.raul.virtualclubs.models.enums.ResponseType;

/**
 * A generic API response record.
 *
 * @param success      Indicates if the operation was successful.
 * @param message      An {@link ErrorType} enum representing the error message if `success` is false,
 *                     or a success message if `success` is true.
 * @param responseType An {@link ResponseType} enum indicating the type of response (e.g., SUCCESS, ERROR, WARNING).
 * @param data         The actual data returned by the API, if any.
 * @param <T>          The type of the data.
 */
public record ApiResponse<T>(boolean success, ErrorType message, ResponseType responseType, T data) {}
