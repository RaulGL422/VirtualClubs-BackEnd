package galindo.raul.virtualclubs.dtos.response;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

/**
 * Data Transfer Object for the registration response.
 *
 * @param accessToken  The JWT access token for authentication.
 * @param refreshToken The token used to refresh the access token.
 * @param email        The email address of the registered user.
 */
public record RegisterResponse(
    @NonNull
    @NotBlank
    String accessToken,
    
    @NonNull
    @NotBlank
    String refreshToken,
    
    @NonNull
    @NotBlank
    String email) {}
