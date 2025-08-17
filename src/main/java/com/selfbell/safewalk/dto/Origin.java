package com.selfbell.safewalk.dto;

import jakarta.validation.constraints.*;

public record Origin(
        @NotNull
        @DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
        @DecimalMax(value = "90.0", message = "위도는 90도 이하여야 합니다")
        double lat,

        @NotNull
        @DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
        @DecimalMax(value = "180.0", message = "경도는 180도 이하여야 합니다")
        @Min(-180) @Max(180)
        double lon
) {
}
