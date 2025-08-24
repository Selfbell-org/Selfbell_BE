package com.selfbell.safewalk.exception;

import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;

public class SessionAccessDeniedException extends ApiException {
    public SessionAccessDeniedException(Long sessionId, Long userId) {
        super(ErrorCode.SESSION_ACCESS_DENIED, "해당 세션에 접근할 권한이 없습니다. 세션 ID: " + sessionId + ", 사용자 ID: " + userId);
    }
}
