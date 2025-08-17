package com.selfbell.global.dto;

public record ErrorResponse(
        String code,
        String message,
        Object data
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null);
    }
    
    public static ErrorResponse of(String code, String message, Object data) {
        return new ErrorResponse(code, message, data);
    }
}