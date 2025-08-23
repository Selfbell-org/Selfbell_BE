package com.selfbell.safewalk.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SessionListResponse(
        List<SessionListItem> sessions
) {
}
