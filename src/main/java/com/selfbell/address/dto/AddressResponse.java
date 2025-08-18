package com.selfbell.address.dto;

import com.selfbell.address.domain.Address;
import lombok.Builder;

@Builder
public record AddressResponse(
        String name,
        String address,
        double lat,
        double lon
) {
    public static AddressResponse from(Address address) {
        return  AddressResponse.builder()
                .name(address.getName())
                .address(address.getAddress())
                .lat(address.getLat().doubleValue())
                .lon(address.getLon().doubleValue())
                .build();
    }
}
