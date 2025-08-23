package com.selfbell.safewalk.dto;

import com.selfbell.safewalk.domain.SafeWalkSession;
import com.selfbell.user.dto.UserResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record SessionListItem(
        UserResponse ward,
        SessionSimpleResponse session
) {
    public static SessionListItem from(SafeWalkSession session) {
        return SessionListItem.builder()
                .ward(UserResponse.from(session.getWard()))
                .session(SessionSimpleResponse.from(session))
                .build();
    }

    public SessionListResponse from (List<SessionListItem> sessions) {
        return SessionListResponse.builder()
                .sessions(sessions)
                .build();

    }
}
