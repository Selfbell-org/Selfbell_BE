package com.selfbell.address.controller;

import com.selfbell.address.dto.AddressCreateRequest;
import com.selfbell.address.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/addresses")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<Void> addAddress(
            @RequestParam Long userId,
            @RequestBody @Valid AddressCreateRequest request
    ) {
        Long addressId = addressService.create(userId, request);
        return ResponseEntity.created(URI.create("/api/v1/addresses/" + addressId)).build();
    }
}
