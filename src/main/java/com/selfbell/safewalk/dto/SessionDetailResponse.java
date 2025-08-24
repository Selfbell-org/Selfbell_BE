package com.selfbell.safewalk.dto;

import com.selfbell.safewalk.domain.SafeWalkGuardian;
import com.selfbell.safewalk.domain.SafeWalkSession;
import com.selfbell.user.dto.UserResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record SessionDetailResponse(
        Long sessionId,
        UserResponse ward,
        LocationResponse origin,
        LocationResponse destination,
        String status,
        String startedAt,
        String endedAt,
        String expectedArrival,
        String timerEnd,
        List<UserResponse> guardians
) {
    public static SessionDetailResponse of(SafeWalkSession session, List<SafeWalkGuardian> guardians) {
        List<UserResponse> guardianResponses = guardians.stream()
                .map(SafeWalkGuardian::getGuardian)
                .map(UserResponse::from)
                .toList();

        return SessionDetailResponse.builder()
                .sessionId(session.getId())
                .ward(UserResponse.from(session.getWard()))
                .origin(LocationResponse.from(session.getOrigin(), session.getOriginAddress()))
                .destination(LocationResponse.from(session.getDestination(), session.getDestinationAddress()))
                .status(session.getSafeWalkStatus().name())
                .startedAt(session.getStartedAt().toString())
                .endedAt(session.getEndedAt() != null ? session.getEndedAt().toString() : null)
                .expectedArrival(session.getExpectedArrival() != null ? session.getExpectedArrival().toString() : null)
                .timerEnd(session.getTimerEnd() != null ? session.getTimerEnd().toString() : null)
                .guardians(guardianResponses)
                .build();
    }
}
