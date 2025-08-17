package com.selfbell.criminal.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.selfbell.criminal.dto.CriminalApiXmlDto;
import com.selfbell.criminal.dto.CriminalCoordDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CriminalApiService {

    @Value("${api.criminal.key}")
    private String serviceKeyEncoded; // 이미 인코딩된 키를 yml에 저장했다고 가정

    @Value("${api.vworld.key}")
    private String vworldKey; // VWorld 키 (미인코딩 문자열)

    private final RestTemplate restTemplate;

    private static final String BASE_URL =
            "https://apis.data.go.kr/1383000/sais/SexualAbuseNoticeHouseNumAddrServiceV2/getSexualAbuseNoticeHouseNumAddrListV2";

    private URI buildUriExactly(String type, String ctpvNm, String sggNm, String umdNm,
                                int pageNo, int numOfRows) {

        // 한글 파라미터만 1회 인코딩
        String ctpvEnc = URLEncoder.encode(ctpvNm, StandardCharsets.UTF_8);
        String sggEnc  = URLEncoder.encode(sggNm,  StandardCharsets.UTF_8);
        String umdEnc  = URLEncoder.encode(umdNm,  StandardCharsets.UTF_8);

        // serviceKey는 인코딩된 값을 그대로 사용 (재인코딩 금지)
        String url = BASE_URL
                + "?serviceKey=" + serviceKeyEncoded
                + "&pageNo=" + pageNo
                + "&numOfRows=" + numOfRows
                + "&type=" + type
                + "&ctpvNm=" + ctpvEnc
                + "&sggNm="  + sggEnc
                + "&umdNm="  + umdEnc;

        // 로그 마스킹
        String safe = url.replaceAll("(serviceKey=)([^&]{8})[^&]*", "$1$2******");
        log.info("[CriminalAPI] 요청 URL: {}", safe);

        return URI.create(url);
    }

    /** XML → DTO (원본 유지) */
    public CriminalApiXmlDto getCriminalDataXml(String ctpvNm, String sggNm, String umdNm) throws Exception {
        URI uri = buildUriExactly("xml", ctpvNm, sggNm, umdNm, 1, 10);

        String xml;
        try {
            xml = restTemplate.getForObject(uri, String.class);
        } catch (RestClientResponseException e) {
            log.error("HTTP {} 에러. 응답 본문:\n{}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw e;
        }

        if (xml == null || xml.isBlank()) throw new IllegalStateException("API 응답이 비어있습니다.");

        String sanitized = xml
                .replace("\uFEFF", "")
                .replaceAll("(?s)<!DOCTYPE.*?>", "")
                .replaceAll("(?s)<!--.*?-->", "");

        XmlMapper xmlMapper = XmlMapper.builder()
                .enable(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL)
                .build();
        xmlMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        CriminalApiXmlDto dto = xmlMapper.readValue(sanitized, CriminalApiXmlDto.class);

        if (dto.getHeader() == null || dto.getBody() == null)
            throw new IllegalStateException("XML 파싱 실패: header/body 누락");

        String code = Objects.toString(dto.getHeader().getResultCode(), "");
        if (!code.equals("00") && !code.equals("0")) {
            throw new IllegalStateException("공공데이터 API 오류: " + dto.getHeader().getResultMsg());
        }

        return dto;
    }

    /** JSON 그대로 반환(원본 유지) */
    public String getCriminalDataJson(String ctpvNm, String sggNm, String umdNm) throws Exception {
        URI uri = buildUriExactly("json", ctpvNm, sggNm, umdNm, 1, 10);
        String json;
        try {
            json = restTemplate.getForObject(uri, String.class);
        } catch (RestClientResponseException e) {
            log.error("HTTP {} 에러. 응답 본문:\n{}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
        if (json == null || json.isBlank()) throw new IllegalStateException("API 응답이 비어있습니다.");
        return json;
    }

    /** (신규) API items → 지번주소 조합 → VWorld 지오코딩 → 좌표 리스트 반환 */
    public List<CriminalCoordDto> getCriminalCoords(String ctpvNm, String sggNm, String umdNm) throws Exception {
        CriminalApiXmlDto xmlDto = getCriminalDataXml(ctpvNm, sggNm, umdNm);
        List<CriminalApiXmlDto.Item> items = Optional.ofNullable(xmlDto.getBody())
                .map(CriminalApiXmlDto.Body::getItems)
                .map(CriminalApiXmlDto.Items::getItem)
                .orElseGet(List::of);

        return items.stream()
                .map(it -> {
                    String sido = coalesce(it.getCtpvNm(), ctpvNm);
                    String sgg  = coalesce(it.getSggNm(),  sggNm);
                    String umd  = coalesce(it.getUmdNm(),  umdNm);
                    String mtn  = defaultIfBlank(it.getMtnYn(), "0");
                    String mno  = defaultIfBlank(it.getMno(), "0");
                    String sno  = defaultIfBlank(it.getSno(), "0");

                    String address = buildParcelAddress(sido, sgg, umd, mtn, mno, sno);

                    Optional<LatLng> ll = geocodeParcel(address);
                    if (ll.isEmpty()) {
                        log.warn("[VWorld] 지오코딩 실패: {}", address);
                        return null; // 이후 filter로 제거
                    }

                    LatLng p = ll.get();
                    return CriminalCoordDto.builder()
                            .address(address)
                            .latitude(p.lat)
                            .longitude(p.lng)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /* ---------- 유틸/지오코딩 ---------- */

    private static String coalesce(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v;
    }

    private static String defaultIfBlank(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }

    /** 지번주소 문자열 생성: "서울특별시 광진구 중곡동 18-109" / 산일 경우 "산18-109" */
    private static String buildParcelAddress(String sido, String sgg, String umd,
                                             String mtnYn, String mno, String sno) {
        String bunji = ("1".equals(mtnYn) ? "산" : "") + mno + (isNonZero(sno) ? "-" + sno : "");
        return String.format("%s %s %s %s", sido.trim(), sgg.trim(), umd.trim(), bunji);
    }

    private static boolean isNonZero(String s) {
        if (s == null || s.isBlank()) return false;
        try { return Integer.parseInt(s) > 0; } catch (NumberFormatException e) { return true; }
    }

    /** VWorld 지번주소 → 좌표 */
    private Optional<LatLng> geocodeParcel(String address) {
        String url = UriComponentsBuilder.fromHttpUrl("https://api.vworld.kr/req/address")
                .queryParam("service", "address")
                .queryParam("request", "getCoord")
                .queryParam("type", "PARCEL")   // 지번
                .queryParam("format", "json")
                .queryParam("crs", "EPSG:4326") // WGS84 (경도/위도)
                .queryParam("key", vworldKey)
                .queryParam("address", address)
                .build(false)
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
        if (resultObj instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            result = asMap(first);
        } else {
            result = asMap(resultObj);
        }
        if (result == null) return Optional.empty();

        Map<?, ?> point = asMap(result.get("point"));
        if (point == null) return Optional.empty();

        BigDecimal lng = toBigDecimal(point.get("x")); // 경도
        BigDecimal lat = toBigDecimal(point.get("y")); // 위도
        if (lng == null || lat == null) return Optional.empty();

        return Optional.of(new LatLng(lat, lng));
    }

    private static Map<?, ?> asMap(Object o) {
        return (o instanceof Map<?, ?> m) ? m : null;
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) return null;
        try { return new BigDecimal(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    /** 내부 표현용 */
    private record LatLng(BigDecimal lat, BigDecimal lng) {}
}
