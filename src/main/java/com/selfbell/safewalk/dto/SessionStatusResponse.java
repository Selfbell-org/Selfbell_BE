package com.selfbell.safewalk.dto;

import com.selfbell.safewalk.domain.SafeWalkSession;
import lombok.Builder;

@Builder
public record SessionStatusResponse(
        Long sessionId,
        String status,
        String topic
) {
    public static SessionStatusResponse from(SafeWalkSession safeWalkSession) {
        return SessionStatusResponse.builder()
                .sessionId(safeWalkSession.getId())
                .status(safeWalkSession.getSafeWalkStatus().name())
                .topic("/topic/safe-walk/" + safeWalkSession.getId())
                .build();
    }
}
