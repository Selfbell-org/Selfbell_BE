package com.selfbell.address.dto;

import com.selfbell.address.domain.Address;

import java.math.BigDecimal;

public record AddressUpdateRequest(
        Long addressId,
        String name,
        String address,
        Double lat,
        Double lon
) {
    public Address applyTo(Address existing) {
        return existing.toBuilder()
                .name(name != null ? name : existing.getName())
                .address(address != null ? address : existing.getAddress())
                .lat(lat != null ? BigDecimal.valueOf(lat) : existing.getLat())
                .lon(lon != null ? BigDecimal.valueOf(lon) : existing.getLon())
                .build();
    }
}
