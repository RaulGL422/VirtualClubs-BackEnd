package galindo.raul.virtualclubs.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record RefreshRequest(
    @NonNull
    @NotBlank(message = "11")
    String refreshToken
) {}
