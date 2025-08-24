package com.selfbell.safewalk.exception;

import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;

public class SelfGuardianNotAllowedException extends ApiException {
    public SelfGuardianNotAllowedException(Long userId) {
        super(ErrorCode.SELF_GUARDIAN_NOT_ALLOWED, "자기 자신을 보호자로 등록할 수 없습니다. 사용자 ID: " + userId);
    }
}