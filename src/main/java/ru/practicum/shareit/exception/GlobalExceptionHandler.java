package ru.practicum.shareit.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(ValidationException e) {
        return e.getMessage();
    }

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public String handleValidationException(MethodArgumentNotValidException e) {
//        // Можно более детально разобрать ошибку, чтобы вернуть конкретное сообщение
//        // Например, взять первое сообщение из binding result
//        String errorMessage = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
//        return errorMessage != null ? errorMessage : "Validation error";
//    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException e) {
        log.warn("Conflict error: {}", e.getMessage());
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("An unexpected error occurred: {}", e.getMessage(), e);
        return new ResponseEntity<>(new ErrorResponse("Internal Server Error: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserNotFoundException.class)
    // @ResponseStatus(HttpStatus.NOT_FOUND) // Это переопределит ResponseEntity, если он есть
    // Лучше использовать ResponseEntity для всех кастомных исключений для единообразия
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        log.warn("User not found: {}", e.getMessage());
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleItemNotFoundException(ItemNotFoundException e) {
        log.warn("Item not found: {}", e.getMessage());
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException e) {
        log.warn("Forbidden: {}", e.getMessage());
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleRuntimeException(RuntimeException e) {
        log.error("Internal server error: {}", e.getMessage(), e);
        return "Internal server error";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) // <-- Аннотация для перехвата
    @ResponseStatus(HttpStatus.BAD_REQUEST) // <-- Возвращаем 400 Bad Request
    public String handleMethodArgumentNotValidException(MethodArgumentNotValidException e) { // <-- Метод с правильным именем и параметром
        // Извлекаем первое сообщение об ошибке для простоты
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error"); // Сообщение по умолчанию, если ничего не найдено
        log.warn("Validation error: {}", errorMessage);
        return errorMessage;
    }

}