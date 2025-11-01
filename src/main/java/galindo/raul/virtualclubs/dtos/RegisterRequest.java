package galindo.raul.virtualclubs.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NonNull;

public record RegisterRequest(
        @NonNull
        @NotBlank(message = "6")
        @Size(min = 6, message = "8")
        String password,

        @NonNull
        @NotBlank(message = "5")
        @Email(message = "9")
        String email
) {}