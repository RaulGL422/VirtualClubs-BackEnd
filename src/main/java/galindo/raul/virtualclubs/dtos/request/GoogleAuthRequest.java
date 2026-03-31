package galindo.raul.virtualclubs.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record GoogleAuthRequest(
    @NonNull
    @NotBlank(message = "FIELD_BLANK")
    String idToken
) {}
