package com.selfbell.contact.dto;

import jakarta.validation.constraints.NotNull;

public record SharePermissionRequest(
        @NotNull Boolean allow
) {}
