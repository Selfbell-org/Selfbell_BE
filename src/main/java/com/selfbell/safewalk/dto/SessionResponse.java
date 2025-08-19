package com.selfbell.safewalk.dto;

import com.selfbell.safewalk.domain.SafeWalkGuardian;
import com.selfbell.safewalk.domain.SafeWalkSession;
import com.selfbell.user.dto.UserResponse;

import java.util.List;

public record SessionResponse(
        Long sessionId,
        UserResponse ward,
        LocationResponse origin,
        LocationResponse destination,
        String status,
        String startedAt,
        String expectedArrival,
        String timerEnd,
        List<UserResponse> guardians

) {
    public static SessionResponse of(SafeWalkSession session, List<SafeWalkGuardian> guardians) {
        List<UserResponse> guardianResponses = guardians.stream()
                .map(SafeWalkGuardian::getGuardian)
                .map(UserResponse::from)
                .toList();

        return new SessionResponse(
                session.getId(),
                UserResponse.from(session.getWard()),
                LocationResponse.from(session.getOrigin(), session.getOriginAddress()),
                LocationResponse.from(session.getDestination(), session.getDestinationAddress()),
                session.getSafeWalkStatus().name(),
                session.getStartedAt().toString(),
                session.getExpectedArrival() != null ? session.getExpectedArrival().toString() : null,
                session.getTimerEnd() != null ? session.getTimerEnd().toString() : null,
                guardianResponses
        );
    }
}
