package com.selfbell.safewalk.exception;

import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;

public class SessionNotActiveException extends ApiException {
    public SessionNotActiveException(Long sessionId) {
        super(ErrorCode.SESSION_NOT_ACTIVE, "세션이 활성화 상태가 아닙니다. 세션 ID: " + sessionId);
    }
}
