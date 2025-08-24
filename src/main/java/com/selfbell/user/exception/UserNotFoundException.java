package com.selfbell.user.exception;

import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException(Long userId) {
        super(ErrorCode.USER_NOT_FOUND, "해당 사용자를 찾을 수 없습니다. 사용자 ID: " + userId);
    }

    public UserNotFoundException(Long userId, String message){
        super(ErrorCode.USER_NOT_FOUND, message + " 사용자 ID: " + userId);
    }
}
