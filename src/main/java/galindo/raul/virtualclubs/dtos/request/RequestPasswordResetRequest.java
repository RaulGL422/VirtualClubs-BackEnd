package galindo.raul.virtualclubs.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record RequestPasswordResetRequest(
    @NonNull
    @NotBlank(message = "FIELD_BLANK")
    @Email(message = "INVALID_EMAIL")
    String email
) {}
