package com.selfbell.safewalk.exception;

public class ActiveSessionExistsException extends RuntimeException {
    public ActiveSessionExistsException() {
        super("해당 유저의 이미 진행중인 세션이 있습니다. 기존 세션을 종료한 후 새 세션을 시작해주세요.");
    }
    
    public ActiveSessionExistsException(Long sessionId) {
        super("해당 유저의 이미 진행중인 세션이 있습니다. 세션 ID: " + sessionId);
    }
}
