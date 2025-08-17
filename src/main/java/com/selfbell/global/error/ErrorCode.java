package com.selfbell.global.error;

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
    SHARE_CHANGE_NOT_ALLOWED(HttpStatus.CONFLICT, "SHARE_CHANGE_NOT_ALLOWED", "ACCEPTED 상태에서만 공유 권한을 변경할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String code, String defaultMessage) {
        this.status = status;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
