package com.selfbell.safewalk.dto;

import com.selfbell.safewalk.domain.SafeWalkSession;
import com.selfbell.safewalk.domain.enums.SafeWalkStatus;
import lombok.Builder;

@Builder
public record SessionSimpleResponse(
        Long id,
        SafeWalkStatus status,
        String addressName,
        String startedAt
) {
    public static SessionSimpleResponse from (SafeWalkSession session){
        return SessionSimpleResponse.builder()
                .id(session.getId())
                .status(session.getSafeWalkStatus())
                .addressName(session.getDestinationName())
                .startedAt(session.getStartedAt().toString())
                .build();
    }
}
