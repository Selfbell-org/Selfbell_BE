package com.selfbell.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FcmConfig {

    @Value("${firebase.service-account-key}")
    private String firebaseConfigPath;
    
    @Value("${firebase.service-account-key-content}")
    private String firebaseConfigContent;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                GoogleCredentials credentials = null;
                
                // 1. 환경변수로 JSON 내용이 설정된 경우 우선 사용
                if (firebaseConfigContent != null && !firebaseConfigContent.trim().isEmpty()) {
                    try (InputStream serviceAccount = new ByteArrayInputStream(firebaseConfigContent.getBytes())) {
                        credentials = GoogleCredentials.fromStream(serviceAccount);
                        log.info("Firebase Admin SDK 초기화 완료 (환경변수 사용)");
                    }
                }
                // 2. 파일이 존재하는 경우 사용
                else if (firebaseConfigPath != null && !firebaseConfigPath.trim().isEmpty()) {
                    ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
                    
                    if (resource.exists()) {
                        try (InputStream serviceAccount = resource.getInputStream()) {
                            credentials = GoogleCredentials.fromStream(serviceAccount);
                            log.info("Firebase Admin SDK 초기화 완료 (파일: {})", firebaseConfigPath);
                        }
                    } else {
                        log.error("Firebase 서비스 계정 키 파일을 찾을 수 없습니다: {}", firebaseConfigPath);
                    }
                }
                
                if (credentials != null) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(credentials)
                            .build();
                    
                    FirebaseApp.initializeApp(options);
                } else {
                    log.warn("Firebase 서비스 계정 키가 설정되지 않았습니다. Firebase 알림이 비활성화됩니다.");
                }
            } else {
                log.info("Firebase Admin SDK가 이미 초기화되어 있습니다.");
            }
        } catch (IOException e) {
            log.error("Firebase Admin SDK 초기화 실패", e);
            log.warn("Firebase 알림이 비활성화됩니다.");
        }
    }
}
