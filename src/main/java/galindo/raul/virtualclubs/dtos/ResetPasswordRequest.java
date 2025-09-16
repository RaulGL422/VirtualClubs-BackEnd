package galindo.raul.virtualclubs.dtos;

public record ResetPasswordRequest(String token, String newPassword) {}