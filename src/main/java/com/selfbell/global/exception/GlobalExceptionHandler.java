package com.selfbell.global.exception;

import com.selfbell.global.dto.ErrorResponse;
import com.selfbell.global.error.ApiException;
import com.selfbell.safewalk.exception.ActiveSessionExistsException;
import com.selfbell.safewalk.exception.SessionAccessDeniedException;
import com.selfbell.safewalk.exception.SessionNotActiveException;
import com.selfbell.safewalk.exception.SessionNotFoundException;
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

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSessionNotFound(SessionNotFoundException e) {
        ErrorResponse error = ErrorResponse.of("SESSION_NOT_FOUND", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(SessionAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleSessionAccessDenied(SessionAccessDeniedException e) {
        ErrorResponse error = ErrorResponse.of("SESSION_ACCESS_DENIED", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(SessionNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleSessionNotActive(SessionNotActiveException e) {
        ErrorResponse error = ErrorResponse.of("SESSION_NOT_ACTIVE", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        ErrorResponse error = ErrorResponse.of("INVALID_ARGUMENT", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException e) {
        var ec = e.getErrorCode();
        var body = ErrorResponse.of(ec.getCode(), e.getMessage());
        return ResponseEntity.status(ec.getStatus()).body(body);
    }
}
