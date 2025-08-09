package galindo.raul.virtualclubs.dtos;

public record ApiResponse<T>(boolean success, String message, ResponseType responseType, T data) {}
