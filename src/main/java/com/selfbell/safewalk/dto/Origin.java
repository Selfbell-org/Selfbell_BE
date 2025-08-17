package com.selfbell.safewalk.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record Origin(
        @Min(-90) @Max(90)
        double lat,

        @Min(-180) @Max(180)
        double lon
) {
}
