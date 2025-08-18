package com.selfbell.address.dto;

import jakarta.validation.constraints.NotNull;

public record AddressCreateRequest(
        @NotNull
        String name,
        @NotNull
        String address,
        @NotNull
        Double lat,
        @NotNull
        Double lon
) {
}
