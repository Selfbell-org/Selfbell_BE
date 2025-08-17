package com.selfbell.safewalk.controller;

import com.selfbell.safewalk.dto.SessionCreateRequest;
import com.selfbell.safewalk.dto.SessionCreateResponse;
import com.selfbell.safewalk.service.SafeWalkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/safe-walks")
@RequiredArgsConstructor
public class SafeWalkSessionController {

    private final SafeWalkService safeWalkService;

    @PostMapping
    public ResponseEntity<SessionCreateResponse> startSession(
            // TODO: 커스텀 어노테이션으로 인증된 사용자 ID를 가져오기, 현재는 우선 RequestParam로 처리
            @RequestParam Long userId,
            @RequestBody @Valid SessionCreateRequest request
    ) {
        SessionCreateResponse response = safeWalkService.createSession(userId, request);
        return ResponseEntity.created(URI.create("/api/safe-walks/" + response.sessionId())).body(response);
    }
}
