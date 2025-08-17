package com.selfbell.safewalk.dto;


import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SessionCreateRequest(
        Origin origin,

        @NotNull
        String originAddress,
        Destination destination,

        @NotNull
        String destinationAddress,

        String expectedArrival,
        Integer timerMinutes,
        List<Long> guardianIds
) {
}
