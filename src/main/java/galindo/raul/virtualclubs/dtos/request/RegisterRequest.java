package galindo.raul.virtualclubs.dtos.request;

import galindo.raul.virtualclubs.models.annotations.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NonNull;

/**
 * Data Transfer Object for user registration requests.
 *
 * @param password The user's password, must be strong and between 6 and 64 characters.
 * @param email    The user's email address, must be a valid email format.
 */
public record RegisterRequest(
        @NonNull
        @NotBlank(message = "PASSWORD_REQUIRED")
        @Size(min = 6, max = 64, message = "PASSWORD_MIN_LENGTH_ERROR")
        @StrongPassword
        String password,

        @NonNull
        @NotBlank(message = "EMAIL_REQUIRED")
        @Email(message = "INVALID_EMAIL_FORMAT")
        String email
) {}