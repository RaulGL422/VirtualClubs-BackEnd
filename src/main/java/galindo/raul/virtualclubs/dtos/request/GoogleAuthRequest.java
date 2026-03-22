package galindo.raul.virtualclubs.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record GoogleAuthRequest(
    @NonNull
    @NotBlank(message = "11")
    String idToken
) {}
