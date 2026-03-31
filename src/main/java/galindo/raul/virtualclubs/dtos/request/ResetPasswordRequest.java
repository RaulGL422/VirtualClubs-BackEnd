package galindo.raul.virtualclubs.dtos.request;

import galindo.raul.virtualclubs.models.annotations.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NonNull;

public record ResetPasswordRequest(
    @NonNull
    @NotBlank(message = "FIELD_BLANK")
    String token,

    @NonNull
    @NotBlank(message = "FIELD_BLANK")
    @Size(min = 6, message = "PASSWORD_TOO_SHORT")
    @StrongPassword
    String newPassword
) {}