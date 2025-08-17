package com.selfbell.safewalk.dto;

import com.selfbell.safewalk.domain.SafeWalkSession;
import com.selfbell.safewalk.domain.enums.SafeWalkStatus;

import java.time.LocalDateTime;

public record SessionEndResponse(
        Long sessionId,
        SafeWalkStatus status,
        LocalDateTime endedAt
) {
    public static SessionEndResponse of(SafeWalkSession session) {
        return new SessionEndResponse(
                session.getId(),
                session.getSafeWalkStatus(),
                session.getEndedAt()
        );
    }
}
