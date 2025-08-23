package com.selfbell.global.exception;

import com.selfbell.global.dto.ErrorResponse;
import com.selfbell.global.error.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException e) {
        log.error("API Exception: {} - {}", e.getErrorCode().getCode(), e.getMessage());
        var ec = e.getErrorCode();
        var body = ErrorResponse.of(ec.getCode(), e.getMessage());
        return ResponseEntity.status(ec.getStatus()).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.error("Validation Error: {}", e.getMessage());
        ErrorResponse error = ErrorResponse.of("INVALID_ARGUMENT", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e) {
        log.error("Unexpected Error: {}", e.getMessage(), e);
        ErrorResponse error = ErrorResponse.of("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
