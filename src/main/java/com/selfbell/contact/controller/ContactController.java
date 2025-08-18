// ContactController
package com.selfbell.contact.controller;

import com.selfbell.contact.dto.*;
import com.selfbell.contact.service.ContactService;
import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;
import com.selfbell.user.domain.User;
import com.selfbell.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final UserRepository userRepository; // ★ 추가

    // JWT subject = phoneNumber 를 ID로 변환
    private Long currentUserId() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || "anonymousUser".equals(String.valueOf(a.getPrincipal()))) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        String phone = a.getName(); // JWT subject = phoneNumber
        User me = userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return me.getId(); // ★ 여기서 ID로 변환
    }

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
