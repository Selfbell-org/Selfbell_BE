package com.selfbell.safewalk.exception;

import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;

public class ActiveSessionExistsException extends ApiException {
    public ActiveSessionExistsException() {
        super(ErrorCode.SESSION_ALREADY_STARTED, "해당 유저의 이미 진행중인 세션이 있습니다. 기존 세션을 종료한 후 새 세션을 시작해주세요.");
    }
    
    public ActiveSessionExistsException(Long sessionId) {
        super(ErrorCode.SESSION_ALREADY_STARTED, "해당 유저의 이미 진행중인 세션이 있습니다. 세션 ID: " + sessionId);
    }
}
