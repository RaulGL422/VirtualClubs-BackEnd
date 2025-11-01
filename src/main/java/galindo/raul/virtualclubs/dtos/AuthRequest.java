package galindo.raul.virtualclubs.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record AuthRequest(
        @NonNull
        @NotBlank(message = "5")
        String email,

        @NonNull
        @NotBlank(message = "6")
        String password
) {}
