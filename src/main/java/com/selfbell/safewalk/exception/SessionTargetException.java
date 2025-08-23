package com.selfbell.safewalk.exception;

import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;

public class SessionTargetException extends ApiException {
    public SessionTargetException(String target) {
        super(ErrorCode.INVALID_SESSION_TARGET, "Target은 'me' 또는 'ward'만 허용됩니다. 입력값: " + target);
    }
}
