package com.selfbell.global.error;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L; // 경고 방지용
    private final ErrorCode errorCode;

    // 기본 메시지는 ErrorCode 내부 메시지 사용
    public ApiException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    // 필요할 때 커스텀 메시지 지정
    public ApiException(ErrorCode errorCode, String overrideMessage) {
        super(overrideMessage);
        this.errorCode = errorCode;
    }
}
