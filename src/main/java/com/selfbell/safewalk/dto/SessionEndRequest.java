package com.selfbell.safewalk.dto;

import jakarta.validation.constraints.NotBlank;

public record SessionEndRequest(
        @NotBlank
        String reason
) {
}
