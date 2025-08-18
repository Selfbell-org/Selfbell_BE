package com.selfbell.address.controller;

import com.selfbell.address.dto.AddressCreateRequest;
import com.selfbell.address.dto.AddressUpdateRequest;
import com.selfbell.address.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.selfbell.global.jwt.JwtTokenProvider.currentUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/addresses")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<Void> addAddress(
            @RequestBody @Valid AddressCreateRequest request
    ) {
        Long userId = currentUserId();
        Long addressId = addressService.create(userId, request);
        return ResponseEntity.created(URI.create("/api/v1/addresses/" + addressId)).build();
    }

    @PatchMapping("/{addressId}")
    public ResponseEntity<Void> updateAddress(
            @PathVariable Long addressId,
            @RequestBody @Valid AddressUpdateRequest request
    ){
        Long userId = currentUserId();
        addressService.update(userId, addressId, request);
        return ResponseEntity.noContent().build();
    }
}
