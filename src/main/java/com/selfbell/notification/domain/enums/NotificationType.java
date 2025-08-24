package com.selfbell.notification.domain.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    SAFE_WALK_STARTED("SAFE_WALK_STARTED", "안전 귀가가 시작되었습니다"),
    SAFE_WALK_ENDED("SAFE_WALK_ENDED", "안전 귀가가 종료되었습니다");

    private final String code;
    private final String message;

    NotificationType(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
