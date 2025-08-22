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

    @Value("${api.vworld.key}")
    private String vworldKey;

    /** 지번주소 → 좌표 (캐시 적용) */
    @Cacheable(cacheNames = "addr2coord", key = "#p0")
    public Optional<LatLng> geocodeParcel(String address) {
        String url = UriComponentsBuilder.fromUriString("https://api.vworld.kr/req/address")
                .queryParam("service", "address")
                .queryParam("request", "getCoord")
                .queryParam("type", "PARCEL")
                .queryParam("format", "json")
                .queryParam("crs", "EPSG:4326")
                .queryParam("key", vworldKey)
                .queryParam("address", address)
                .build(false)
                .toUriString();

        try {
            Map<?, ?> res = restTemplate.getForObject(url, Map.class);
            if (res == null) return Optional.empty();

            Map<?, ?> response = asMap(res.get("response"));
            if (response == null || !"OK".equals(String.valueOf(response.get("status")))) return Optional.empty();

            Object resultObj = response.get("result");
            Map<?, ?> result = (resultObj instanceof java.util.List<?> list && !list.isEmpty())
                    ? asMap(list.get(0)) : asMap(resultObj);
            if (result == null) return Optional.empty();

            Map<?, ?> point = asMap(result.get("point"));
            if (point == null) return Optional.empty();

            BigDecimal lon = toBigDecimal(point.get("x"));
            BigDecimal lat = toBigDecimal(point.get("y"));
            if (lon == null || lat == null) return Optional.empty();

            return Optional.of(new LatLng(lat, lon));
        } catch (Exception e) {
            log.error("[VWorld] HTTP 오류: {}, address={}", e.getMessage(), address);
            return Optional.empty();
        }
    }

    /** 위/경도 → 행정구역(캐시 키: 소수4자리 라운딩) */
    @Cacheable(cacheNames = "reverse_admin", key = "T(java.lang.String).format('%.4f,%.4f', #p0, #p1)")
    public Optional<AdminRegion> reverseGeocode(double lat, double lon) {
        return reverseGeocodeInternal(lat, lon);
    }

    /** 중심점 전용: 캐시 미사용(정확) */
    public Optional<AdminRegion> reverseGeocodeRaw(double lat, double lon) {
        return reverseGeocodeInternal(lat, lon);
    }

    private Optional<AdminRegion> reverseGeocodeInternal(double lat, double lon) {
        String url = UriComponentsBuilder.fromUriString("https://api.vworld.kr/req/address")
                .queryParam("service", "address")
                .queryParam("request", "getAddress")
                .queryParam("format", "json")
                .queryParam("crs", "EPSG:4326")
                .queryParam("point", lon + "," + lat)  // (lon,lat)
                .queryParam("type", "both")
                .queryParam("key", vworldKey)
                .build(false)
                .toUriString();

        try {
            Map<?, ?> res = restTemplate.getForObject(url, Map.class);
            if (res == null) return Optional.empty();

            Map<?, ?> response = asMap(res.get("response"));
            if (response == null || !"OK".equals(String.valueOf(response.get("status")))) return Optional.empty();

            Object resultObj = response.get("result");
            Map<?, ?> first = (resultObj instanceof java.util.List<?> list && !list.isEmpty())
                    ? asMap(list.get(0)) : asMap(resultObj);
            if (first == null) return Optional.empty();

            Map<?, ?> structure = asMap(first.get("structure"));
            if (structure == null) return Optional.empty();

            String level1 = String.valueOf(structure.get("level1"));
            String level2 = String.valueOf(structure.get("level2"));
            String level3 = String.valueOf(structure.get("level3"));
            return Optional.of(new AdminRegion(level1, level2, level3));
        } catch (Exception e) {
            log.error("[VWorld] reverse HTTP 오류: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static Map<?, ?> asMap(Object o) { return (o instanceof Map<?, ?> m) ? m : null; }
    private static BigDecimal toBigDecimal(Object o) {
        try { return (o == null) ? null : new BigDecimal(String.valueOf(o)); }
        catch (Exception e) { return null; }
    }

    public record LatLng(BigDecimal lat, BigDecimal lon) {}
    public record AdminRegion(String ctpvNm, String sggNm, String umdNm) {}
}
