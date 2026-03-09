package galindo.raul.virtualclubs.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NonNull;

public record ResetPasswordRequest(
    @NonNull
    @NotBlank(message = "11")
    String token,
    
    @NonNull
    @NotBlank(message = "6")
    @Size(min = 6, message = "8")
    String newPassword
) {}