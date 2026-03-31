package galindo.raul.virtualclubs.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record RefreshRequest(
    @NonNull
    @NotBlank(message = "FIELD_BLANK")
    String refreshToken
) {}
