package com.selfbell.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("해당 사용자를 찾을 수 없습니다. 사용자 ID: " + userId);
    }
}
