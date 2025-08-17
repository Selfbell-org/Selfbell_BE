package com.selfbell.safewalk.exception;

public class SessionNotActiveException extends RuntimeException {
    public SessionNotActiveException(Long sessionId) {
        super("세션이 활성화 상태가 아닙니다. 세션 ID: " + sessionId);
    }
}