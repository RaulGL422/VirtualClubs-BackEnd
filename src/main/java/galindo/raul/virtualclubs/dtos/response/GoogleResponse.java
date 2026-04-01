package galindo.raul.virtualclubs.dtos.response;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record GoogleResponse(
    @NonNull
    @NotBlank
    String accessToken,
    
    @NonNull
    @NotBlank
    String refreshToken
) {}
