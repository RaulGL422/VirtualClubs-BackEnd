package galindo.raul.virtualclubs.dtos.response;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

/**
 * Data Transfer Object for the token refresh response.
 *
 * @param accessToken  The new JWT access token.
 * @param refreshToken The new JWT refresh token.
 */
public record RefreshResponse(
    @NonNull
    @NotBlank
    String accessToken,
    
    @NonNull
    @NotBlank
    String refreshToken
) {}
