package com.selfbell.address.dto;

import java.util.List;

public record AddressListResponse(
        List<AddressResponse> items
) {
    public static AddressListResponse of(List<AddressResponse> items) {
        return new AddressListResponse(items);
    }
}
