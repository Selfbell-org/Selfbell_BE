package com.selfbell.safewalk.exception;

public class SessionAccessDeniedException extends RuntimeException {
    public SessionAccessDeniedException(Long sessionId, Long userId) {
        super("해당 세션에 접근할 권한이 없습니다. 세션 ID: " + sessionId + ", 사용자 ID: " + userId);
    }
}
