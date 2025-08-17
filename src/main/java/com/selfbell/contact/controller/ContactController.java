package com.selfbell.contact.controller;

import com.selfbell.contact.dto.*;
import com.selfbell.contact.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    // JWT의 subject(현재 구성)에 userId(문자열) 들어옴 -> Long 변환
    private Long currentUserId(Authentication auth) {
        return Long.parseLong(auth.getName());
    }

    @PostMapping("/requests")
    public ResponseEntity<ContactCreateResponseDTO> createRequest(
            Authentication auth,
            @RequestBody @Valid ContactRequestCreateDTO request
    ) {
        var meId = currentUserId(auth);
        var resp = contactService.createRequest(meId, request);
        return ResponseEntity.status(201).body(resp);
    }

    @PostMapping("/{contactId}/accept")
    public ResponseEntity<ContactAcceptResponseDTO> accept(
            Authentication auth,
            @PathVariable Long contactId
    ) {
        var meId = currentUserId(auth);
        var resp = contactService.accept(meId, contactId);
        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/{id}/share-permission")
    public ResponseEntity<ContactAcceptResponseDTO> updateSharePermission(
            @PathVariable Long id,
            @RequestBody @Valid SharePermissionRequest request
    ) {
        return ResponseEntity.ok(contactService.updateSharePermission(id, request));
    }

    @GetMapping
    public ResponseEntity<ContactListResponseDTO> list(
            Authentication auth,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var meId = currentUserId(auth);
        var pageable = PageRequest.of(page, size, Sort.by("id").descending());
        var resp = contactService.list(meId, status, pageable);
        return ResponseEntity.ok(resp);
    }
}
