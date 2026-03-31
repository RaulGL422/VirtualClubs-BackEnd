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
        @NotBlank(message = "FIELD_BLANK")
        @Size(min = 6, max = 64, message = "PASSWORD_TOO_SHORT")
        @StrongPassword
        String password,

        @NonNull
        @NotBlank(message = "FIELD_BLANK")
        @Email(message = "INVALID_EMAIL")
        String email
) {}