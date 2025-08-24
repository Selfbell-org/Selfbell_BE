package com.selfbell.criminal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

@Configuration
@EnableCaching
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        var cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(200)      // 전체 커넥션 상한
                .setMaxConnPerRoute(50)    // 엔드포인트별 상한
                .build();

        // 풀에서 커넥션 빌려오는 최대 대기시간 등 기본 요청 설정
        RequestConfig defaultConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(1500))
                .setResponseTimeout(Timeout.ofMilliseconds(3000))
                .setConnectionRequestTimeout(Timeout.ofSeconds(2)) // 풀 고갈 시 대기 상한
                .build();

        CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(defaultConfig)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(30))     // HttpClient5 시그니처
                .build();

        var factory = new HttpComponentsClientHttpRequestFactory(client);
        // Spring 쪽 타임아웃도 한 번 더(겹쳐 설정해도 문제 없음)
        factory.setConnectTimeout(Duration.ofMillis(1500));
        factory.setReadTimeout(Duration.ofMillis(3000));

        return new RestTemplate(factory);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
