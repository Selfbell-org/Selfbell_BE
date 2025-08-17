package com.selfbell.global.exception;

import com.selfbell.global.dto.ErrorResponse;
import com.selfbell.safewalk.exception.ActiveSessionExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ActiveSessionExistsException.class)
    public ResponseEntity<ErrorResponse> handleActiveSessionExists(ActiveSessionExistsException e) {
        ErrorResponse error = ErrorResponse.of("ACTIVE_SESSION_EXISTS", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        ErrorResponse error = ErrorResponse.of("INVALID_ARGUMENT", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
}
