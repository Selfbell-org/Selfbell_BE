package com.selfbell.criminal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class VWorldClient {

    private final RestTemplate restTemplate;

    @Value("${api.vworld.key}") // application.yml: api.vworld.key=발급키
    private String vworldKey;

    /** 지번주소 → 좌표 (캐시 적용) */
    @Cacheable(cacheNames = "addr2coord", key = "#p0") // key=#address 대신 #p0 사용(메타데이터 이슈 회피)
    public Optional<LatLng> geocodeParcel(String address) {
        String url = UriComponentsBuilder.fromHttpUrl("https://api.vworld.kr/req/address")
                .queryParam("service", "address")
                .queryParam("request", "getCoord")
                .queryParam("type", "PARCEL")
                .queryParam("format", "json")
                .queryParam("crs", "EPSG:4326")
                .queryParam("key", vworldKey)
                .queryParam("address", address)
                .build(false) // 반드시 인코딩 수행
                .toUriString();

        Map<?, ?> res;
        try {
            res = restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("[VWorld] HTTP 오류: {}, address={}", e.getMessage(), address);
            return Optional.empty();
        }
        if (res == null) return Optional.empty();

        Map<?, ?> response = asMap(res.get("response"));
        if (response == null || !"OK".equals(String.valueOf(response.get("status")))) {
            return Optional.empty();
        }

        Object resultObj = response.get("result");
        Map<?, ?> result = null;
        if (resultObj instanceof java.util.List<?> list && !list.isEmpty()) {
            result = asMap(list.get(0));
        } else {
            result = asMap(resultObj);
        }
        if (result == null) return Optional.empty();

        Map<?, ?> point = asMap(result.get("point"));
        if (point == null) return Optional.empty();

        BigDecimal lng = toBigDecimal(point.get("x"));
        BigDecimal lat = toBigDecimal(point.get("y"));
        if (lng == null || lat == null) return Optional.empty();

        return Optional.of(new LatLng(lat, lng));
    }

    private static Map<?, ?> asMap(Object o) { return (o instanceof Map<?, ?> m) ? m : null; }

    private static BigDecimal toBigDecimal(Object o) {
        try { return (o == null) ? null : new BigDecimal(String.valueOf(o)); }
        catch (Exception e) { return null; }
    }

    /** 좌표 레코드 */
    public record LatLng(BigDecimal lat, BigDecimal lng) {}
}
