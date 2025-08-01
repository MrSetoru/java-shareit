package ru.practicum.shareit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {
    private final String message;

    public ValidationException(String message) {
        super(message);
        this.message = message;
    }
}