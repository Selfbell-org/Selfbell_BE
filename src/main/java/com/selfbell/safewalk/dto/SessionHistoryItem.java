package com.selfbell.safewalk.dto;

import com.selfbell.safewalk.domain.SafeWalkSession;
import com.selfbell.user.dto.UserResponse;
import lombok.Builder;


@Builder
public record SessionHistoryItem(
        UserResponse ward,
        SessionSimpleResponse session
) {
    public static SessionHistoryItem from(SafeWalkSession session) {
        return SessionHistoryItem.builder()
                .ward(UserResponse.from(session.getWard()))
                .session(SessionSimpleResponse.from(session))
                .build();
    }
}
