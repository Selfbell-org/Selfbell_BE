package com.selfbell.safewalk.dto;

import com.selfbell.safewalk.domain.SafeWalkSession;
import com.selfbell.safewalk.domain.enums.SafeWalkStatus;

public record SessionCreateResponse(
        Long sessionId,
        SafeWalkStatus safeWalkStatus,
        String startedAt,
        String expectedArrival,
        String timerEnd,
        String topic
) {
    public static SessionCreateResponse from(SafeWalkSession session) {
        return new SessionCreateResponse(
                session.getId(),
                session.getSafeWalkStatus(),
                session.getStartedAt().toString(),
                session.getExpectedArrival() != null ? session.getExpectedArrival().toString() : null,
                session.getTimerEnd() != null ? session.getTimerEnd().toString() : null,
                "/topic/safe-walks/" + session.getId()
        );
    }
}
