package com.selfbell.global.error;

import com.selfbell.global.dto.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    SELF_REQUEST_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "SELF_REQUEST_NOT_ALLOWED", "자기 자신에게는 요청할 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "해당 전화번호로 가입된 사용자가 없습니다."),
    CONTACT_ALREADY_EXISTS(HttpStatus.CONFLICT, "CONTACT_ALREADY_EXISTS", "이미 관계가 존재합니다."),
    NOT_PARTICIPANT(HttpStatus.FORBIDDEN, "NOT_PARTICIPANT", "해당 관계의 당사자만 변경할 수 있습니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, "INVALID_STATUS_TRANSITION", "PENDING 상태에서만 수락할 수 있습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "유효하지 않은 입력입니다."),
    CONTACT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTACT_NOT_FOUND", "해당 연락처 관계를 찾을 수 없습니다."),
    INVALID_STATUS_FILTER(HttpStatus.BAD_REQUEST, "INVALID_STATUS_FILTER", "status는 PENDING 또는 ACCEPTED만 허용됩니다."),
    SHARE_CHANGE_NOT_ALLOWED(HttpStatus.CONFLICT, "SHARE_CHANGE_NOT_ALLOWED", "ACCEPTED 상태에서만 공유 권한을 변경할 수 있습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),

    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_INVALID", "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "만료된 리프레시 토큰입니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.CONFLICT, "REFRESH_TOKEN_MISMATCH", "저장된 리프레시 토큰과 일치하지 않습니다."),
    REFRESH_TOKEN_REVOKED(HttpStatus.FORBIDDEN, "REFRESH_TOKEN_REVOKED", "취소된 리프레시 토큰입니다."),
    REFRESH_TOKEN_REUSED(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_REUSED", "재사용이 감지된 리프레시 토큰입니다."),

    // AddressNotFoundException 예외 처리에 사용되는 ErrorCode
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND", "해당 주소를 찾을 수 없습니다."),
    ADDRESS_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "ADDRESS_UNAUTHORIZED", "주소에 대한 권한이 없습니다."),

    // Session 관련 예외 처리에 사용되는 ErrorCode
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "세션을 찾을 수 없습니다."),
    SESSION_ALREADY_ENDED(HttpStatus.CONFLICT, "SESSION_ALREADY_ENDED", "이미 종료된 세션입니다."),
    SESSION_ALREADY_STARTED(HttpStatus.CONFLICT, "SESSION_ALREADY_STARTED", "이미 시작된 세션입니다."),
    SESSION_ARRIVAL_TIME_PASSED(HttpStatus.BAD_REQUEST, "SESSION_ARRIVAL_TIME_PASSED", "도착 예정 시간이 현재 시간보다 이전일 수 없습니다."),
    SESSION_NOT_ACTIVE(HttpStatus.CONFLICT, "SESSION_NOT_ACTIVE", "활성화되지 않은 세션입니다."),
    SESSION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "SESSION_ACCESS_DENIED", "세션에 대한 접근 권한이 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String code, String defaultMessage) {
        this.status = status;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    // 헬퍼: 기본 메시지로 ErrorResponse 만들기
    public ErrorResponse toResponse() {
        return ErrorResponse.of(this.code, this.defaultMessage);
    }

    // 헬퍼: 커스텀 메시지로 ErrorResponse 만들기
    public ErrorResponse toResponse(String overrideMessage) {
        return ErrorResponse.of(this.code, overrideMessage);
    }
}
