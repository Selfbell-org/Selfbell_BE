package com.selfbell.safewalk.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record SessionCreateRequest(
        Origin origin,

        @NotNull
        String originAddress,
        Destination destination,

        @NotNull
        String destinationAddress,

        // TODO: DateTimeFormatter
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$",
                message = "Expected format: yyyy-MM-dd'T'HH:mm")
        String expectedArrival,
        int timerMinutes,
        List<Long> guardiansIds
) {
}
