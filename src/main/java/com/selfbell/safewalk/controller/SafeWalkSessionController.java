package com.selfbell.safewalk.controller;

import com.selfbell.safewalk.dto.SessionCreateRequest;
import com.selfbell.safewalk.dto.SessionCreateResponse;
import com.selfbell.safewalk.dto.SessionEndRequest;
import com.selfbell.safewalk.dto.SessionEndResponse;
import com.selfbell.safewalk.service.SafeWalkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/safe-walks")
@RequiredArgsConstructor
public class SafeWalkSessionController {

    private final SafeWalkService safeWalkService;

    @PostMapping
    public ResponseEntity<SessionCreateResponse> startSession(
            @RequestParam Long userId, // TODO: 인증 구현 후 @CurrentUser로 변경
            @RequestBody @Valid SessionCreateRequest request
    ) {
        SessionCreateResponse response = safeWalkService.createSession(userId, request);
        return ResponseEntity.created(URI.create("/api/safe-walks/" + response.sessionId())).body(response);
    }

    @PutMapping("/{sessionId}/end")
    public ResponseEntity<SessionEndResponse> endSession(
            @PathVariable Long sessionId,
            @RequestParam Long userId, // TODO: 인증 구현 후 @CurrentUser로 변경
            @RequestBody @Valid SessionEndRequest request
    ) {
        SessionEndResponse response = safeWalkService.endSession(sessionId, userId, request);
        return ResponseEntity.ok(response);
    }
}
