package com.selfbell.contact.dto;

import jakarta.validation.constraints.NotNull;

public record ContactSharePermissionRequestDTO(
        @NotNull(message = "allow 값은 true/false 여야 합니다.")
        Boolean allow
) {}
