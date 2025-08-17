package com.selfbell.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApi(ApiException e) {
        var ec = e.getErrorCode();
        return ResponseEntity
                .status(ec.getStatus())
                .body(Map.of(
                        "status", ec.getStatus().value(),
                        "error", ec.getStatus().getReasonPhrase(),
                        "code", ec.getCode(),
                        "message", e.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e) {
        var ec = ErrorCode.INVALID_INPUT;
        String msg = e.getBindingResult().getAllErrors().stream()
                .findFirst().map(err -> err.getDefaultMessage()).orElse(ec.getDefaultMessage());
        return ResponseEntity
                .status(ec.getStatus())
                .body(Map.of(
                        "status", ec.getStatus().value(),
                        "error", ec.getStatus().getReasonPhrase(),
                        "code", ec.getCode(),
                        "message", msg
                ));
    }
}
