package galindo.raul.virtualclubs.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

/**
 * Data Transfer Object for authentication requests.
 * @param email The user's email address, must not be blank.
 * @param password The user's password, must not be blank.
 */
public record AuthRequest(
        @NonNull
        @NotBlank(message = "EMAIL_REQUIRED")
        String email,

        @NonNull
        @NotBlank(message = "PASSWORD_REQUIRED")
        String password
) {}
