package com.selfbell.safewalk.exception;

import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;

public class SessionNotFoundException extends ApiException {
    public SessionNotFoundException(Long sessionId) {
        super(ErrorCode.SESSION_NOT_FOUND,"세션을 찾을 수 없습니다. 세션 ID: " + sessionId);
    }
}
