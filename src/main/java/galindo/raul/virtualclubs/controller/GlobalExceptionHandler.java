package galindo.raul.virtualclubs.controller;

import galindo.raul.virtualclubs.dtos.ApiResponse;
import galindo.raul.virtualclubs.dtos.ResponseType;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        String message = errors.getFirst();

        return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, message, ResponseType.ERROR, null)
        );
    }
}