package com.selfbell.safewalk.dto;

import com.selfbell.safewalk.domain.SafeWalkSession;
import com.selfbell.safewalk.domain.enums.SafeWalkStatus;

import java.time.LocalDateTime;

public record SessionCreateResponse(
        Long sessionId,
        SafeWalkStatus safeWalkStatus,
        LocalDateTime startedAt,
        LocalDateTime expectedArrival,
        LocalDateTime timerEnd,
        String topic
) {
    public static SessionCreateResponse from(SafeWalkSession session) {
        return new SessionCreateResponse(
                session.getId(),
                session.getSafeWalkStatus(),
                session.getStartedAt(),
                session.getExpectedArrival(),
                session.getTimerEnd(),
                "/topic/safe-walks/" + session.getId()
        );
    }
}
