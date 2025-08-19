package com.selfbell.contact.controller;

import com.selfbell.contact.dto.*;
import com.selfbell.contact.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.selfbell.global.jwt.JwtTokenProvider.currentUserId;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/requests")
    public ResponseEntity<ContactCreateResponseDTO> createRequest(@RequestBody @Valid ContactRequestCreateDTO request) {
        Long meId = currentUserId();
        var resp = contactService.createRequest(meId, request);
        return ResponseEntity.status(201).body(resp);
    }

    @PostMapping("/{contactId}/accept")
    public ResponseEntity<ContactAcceptResponseDTO> accept(@PathVariable Long contactId) {
        Long meId = currentUserId();
        var resp = contactService.accept(meId, contactId);
        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/{id}/share-permission")
    public ResponseEntity<ContactAcceptResponseDTO> updateSharePermission(
            @PathVariable Long id, @RequestBody @Valid SharePermissionRequest request) {
        return ResponseEntity.ok(contactService.updateSharePermission(id, request));
    }

    @GetMapping
    public ResponseEntity<ContactListResponseDTO> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long meId = currentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        var resp = contactService.list(meId, status, pageable);
        return ResponseEntity.ok(resp);
    }
}
