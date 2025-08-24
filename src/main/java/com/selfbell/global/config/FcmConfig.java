package com.selfbell.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FcmConfig {

    @Value("${firebase.service-account-key}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
                
                if (!resource.exists()) {
                    log.error("Firebase 서비스 계정 키 파일을 찾을 수 없습니다: {}", firebaseConfigPath);
                    return;
                }

                try (InputStream serviceAccount = resource.getInputStream()) {
                    GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(credentials)
                            .build();

                    FirebaseApp.initializeApp(options);
                    log.info("Firebase Admin SDK 초기화 완료 (파일: {})", firebaseConfigPath);
                }
            }
        } catch (IOException e) {
            log.error("Firebase Admin SDK 초기화 실패", e);
        }
    }
}
