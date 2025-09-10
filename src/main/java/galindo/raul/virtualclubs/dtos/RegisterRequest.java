package galindo.raul.virtualclubs.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NonNull;

public record RegisterRequest(
        @NotBlank(message = "password_required")
        @Size(min = 6, message = "password_min_length")
        String password,

        @NonNull
        @NotBlank(message = "email_required")
        @Email(message = "email_valid")
        String email
) {}