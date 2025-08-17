package com.selfbell.safewalk.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/websocket-test")
@RequiredArgsConstructor
public class WebSocketTestController {

    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/broadcast/{sessionId}")
    public Map<String, Object> testBroadcast(@PathVariable Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String topic = "/topic/safe-walks/" + sessionId;
            Map<String, Object> testMessage = Map.of(
                "type", "TEST_MESSAGE",
                "sessionId", sessionId,
                "message", "STOMP 브로드캐스트 테스트 메시지",
                "timestamp", LocalDateTime.now().toString()
            );
            
            log.info("🧪 테스트 브로드캐스트 시작 - Topic: {}", topic);
            
            messagingTemplate.convertAndSend(topic, testMessage);
            
            result.put("success", true);
            result.put("topic", topic);
            result.put("message", "브로드캐스트 성공");
            result.put("testMessage", testMessage);
            
            log.info("✅ 테스트 브로드캐스트 완료");
            
        } catch (Exception e) {
            log.error("❌ 테스트 브로드캐스트 실패", e);
            
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "브로드캐스트 실패");
        }
        
        return result;
    }

    @GetMapping("/status")
    public Map<String, Object> getWebSocketStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 간단한 상태 정보 반환
            status.put("webSocketEnabled", true);
            status.put("stompEnabled", true);
            status.put("endpoint", "/ws");
            status.put("testPage", "/websocket-test.html");
            status.put("message", "WebSocket/STOMP 서비스가 활성화되어 있습니다");
            
        } catch (Exception e) {
            status.put("webSocketEnabled", false);
            status.put("error", e.getMessage());
        }
        
        return status;
    }
}