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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

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
    private String serviceKeyEncoded; // 인코딩된 키 그대로 사용

    private final RestTemplate restTemplate;
    private final VWorldClient vworldClient;

    private static final String BASE_URL =
            "https://apis.data.go.kr/1383000/sais/SexualAbuseNoticeHouseNumAddrServiceV2/getSexualAbuseNoticeHouseNumAddrListV2";

    /* =========================
       공개 메서드 (시·구만 받는 오버로드)
       ========================= */
    public CriminalApiXmlDto getCriminalDataXml(String ctpvNm, String sggNm) throws Exception {
        return getCriminalDataXml(ctpvNm, sggNm, null);
    }

    public String getCriminalDataJson(String ctpvNm, String sggNm) throws Exception {
        return getCriminalDataJson(ctpvNm, sggNm, null);
    }

    /** 시·구 전체를 지오코딩하여 좌표만 반환 */
    public List<CriminalCoordDto> getCriminalCoords(String ctpvNm, String sggNm) throws Exception {
        List<CriminalApiXmlDto.Item> items = fetchAllItemsOfSgg(ctpvNm, sggNm);
        Set<String> uniq = new LinkedHashSet<>();

        return items.stream()
                .map(it -> {
                    String addr = buildParcelAddress(
                            coalesce(it.getCtpvNm(), ctpvNm),
                            coalesce(it.getSggNm(),  sggNm),
                            it.getUmdNm(),
                            defaultIfBlank(it.getMtnYn(), "0"),
                            defaultIfBlank(it.getMno(), "0"),
                            defaultIfBlank(it.getSno(), "0")
                    );
                    if (!uniq.add(addr)) return null; // 중복 스킵

                    return vworldClient.geocodeParcel(addr)
                            .map(p -> CriminalCoordDto.builder()
                                    .address(addr)
                                    .latitude(p.lat())
                                    .longitude(p.lng())
                                    .distanceMeters(0L)
                                    .build())
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** 최종: 위도/경도/반경을 받아 반경 내 결과(거리 포함)를 반환 */
    public List<CriminalCoordDto> getNearbyCoords(double lat, double lng, int radiusMeters) throws Exception {
        // 1) 역지오코딩으로 시/구 추출(없으면 빈 리스트 반환)
        var regionOpt = vworldClient.reverseGeocode(lat, lng);
        if (regionOpt.isEmpty()) return List.of();
        var region = regionOpt.get();

        // 2) 해당 구 전체 아이템 수집
        List<CriminalApiXmlDto.Item> items = fetchAllItemsOfSgg(region.ctpvNm(), region.sggNm());

        // 3) 주소 중복 제거 → 지오코딩 → 거리 계산 → 반경 필터 → 거리순 정렬
        Set<String> uniq = new LinkedHashSet<>();
        return items.stream()
                .map(it -> {
                    String addr = buildParcelAddress(
                            coalesce(it.getCtpvNm(), region.ctpvNm()),
                            coalesce(it.getSggNm(),  region.sggNm()),
                            it.getUmdNm(),
                            defaultIfBlank(it.getMtnYn(), "0"),
                            defaultIfBlank(it.getMno(), "0"),
                            defaultIfBlank(it.getSno(), "0")
                    );
                    if (!uniq.add(addr)) return null;

                    return vworldClient.geocodeParcel(addr).map(p -> {
                        double d = haversineMeters(lat, lng,
                                p.lat().doubleValue(), p.lng().doubleValue());
                        if (d > radiusMeters) return null;
                        return CriminalCoordDto.builder()
                                .address(addr)
                                .latitude(p.lat())
                                .longitude(p.lng())
                                .distanceMeters(Math.round(d))
                                .build();
                    }).orElse(null);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(CriminalCoordDto::getDistanceMeters))
                .collect(Collectors.toList());
    }

    /* =========================
       기존 3파라미터 API 호출 (xml/json)
       ========================= */
    private URI buildUriExactly(String type, String ctpvNm, String sggNm, String umdNm,
                                int pageNo, int numOfRows) {

        String ctpvEnc = URLEncoder.encode(ctpvNm, StandardCharsets.UTF_8);
        String sggEnc  = URLEncoder.encode(sggNm,  StandardCharsets.UTF_8);

        StringBuilder sb = new StringBuilder(BASE_URL)
                .append("?serviceKey=").append(serviceKeyEncoded)
                .append("&pageNo=").append(pageNo)
                .append("&numOfRows=").append(numOfRows)
                .append("&type=").append(type)
                .append("&ctpvNm=").append(ctpvEnc)
                .append("&sggNm=").append(sggEnc);

        String umdNorm = normalizeNullable(umdNm);
        if (umdNorm != null) {
            String umdEnc = URLEncoder.encode(umdNorm, StandardCharsets.UTF_8);
            sb.append("&umdNm=").append(umdEnc);
        }

        String url = sb.toString();
        String safe = url.replaceAll("(serviceKey=)([^&]{8})[^&]*", "$1$2******");
        log.info("[CriminalAPI] 요청 URL: {}", safe);
        return URI.create(url);
    }

    public CriminalApiXmlDto getCriminalDataXml(String ctpvNm, String sggNm, String umdNm) throws Exception {
        URI uri = buildUriExactly("xml", ctpvNm, sggNm, umdNm, 1, 10);
        String xml;
        try {
            xml = restTemplate.getForObject(uri, String.class);
        } catch (RestClientResponseException e) {
            log.error("HTTP {} 에러. 응답 본문:\n{}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw e;
        }
        if (xml == null || xml.isBlank()) throw new IllegalStateException("API 응답이 비어있습니다.");

        String sanitized = xml.replace("\uFEFF", "")
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

        String code = String.valueOf(dto.getHeader().getResultCode());
        if (!code.equals("00") && !code.equals("0")) {
            throw new IllegalStateException("공공데이터 API 오류: " + dto.getHeader().getResultMsg());
        }
        return dto;
    }

    public String getCriminalDataJson(String ctpvNm, String sggNm, String umdNm) throws Exception {
        URI uri = buildUriExactly("json", ctpvNm, sggNm, umdNm, 1, 10);
        String json;
        try {
            json = restTemplate.getForObject(uri, String.class);
        } catch (RestClientResponseException e) {
            log.error("HTTP {} 에러. 응답 본문:\n{}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw e;
        }
        if (json == null || json.isBlank()) throw new IllegalStateException("API 응답이 비어있습니다.");
        return json;
    }

    /* =========================
       ‘구 전체(모든 동)’ 페이지네이션 수집
       ========================= */
    private List<CriminalApiXmlDto.Item> fetchAllItemsOfSgg(String ctpvNm, String sggNm) throws Exception {
        final int PAGE_SIZE = 100;
        int pageNo = 1;
        int expectedTotal = Integer.MAX_VALUE;
        List<CriminalApiXmlDto.Item> acc = new ArrayList<>();

        while (acc.size() < expectedTotal) {
            URI uri = buildUriExactly("xml", ctpvNm, sggNm, null /* umd 생략 */, pageNo, PAGE_SIZE);

            String xml;
            try {
                xml = restTemplate.getForObject(uri, String.class);
            } catch (RestClientResponseException e) {
                log.error("HTTP {} 에러(page={}):\n{}", e.getStatusCode().value(), pageNo, e.getResponseBodyAsString());
                throw e;
            }
            if (xml == null || xml.isBlank())
                throw new IllegalStateException("API 응답이 비어있습니다. page=" + pageNo);

            String sanitized = xml.replace("\uFEFF", "")
                    .replaceAll("(?s)<!DOCTYPE.*?>", "")
                    .replaceAll("(?s)<!--.*?-->", "");

            XmlMapper xmlMapper = XmlMapper.builder()
                    .enable(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL)
                    .build();
            xmlMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            CriminalApiXmlDto dto = xmlMapper.readValue(sanitized, CriminalApiXmlDto.class);
            if (dto.getHeader() == null || dto.getBody() == null)
                throw new IllegalStateException("XML 파싱 실패: header/body 누락, page=" + pageNo);

            String code = String.valueOf(dto.getHeader().getResultCode());
            if (!code.equals("00") && !code.equals("0"))
                throw new IllegalStateException("공공데이터 API 오류(page=" + pageNo + "): " + dto.getHeader().getResultMsg());

            var body = dto.getBody();
            expectedTotal = Optional.ofNullable(body.getTotalCount()).orElse(0);
            var list = Optional.ofNullable(body.getItems())
                    .map(CriminalApiXmlDto.Items::getItem)
                    .orElseGet(List::of);

            acc.addAll(list);

            if (list.isEmpty()) break;
            pageNo++;
            if (pageNo > 200) { // 안전 한도
                log.warn("[CriminalAPI] Page limit reached ({}). Stop fetching.", pageNo);
                break;
            }
        }
        return acc;
    }

    /* ---------- 유틸 ---------- */

    private static String normalizeNullable(String s) {
        if (s == null) return null;
        String t = s.trim();
        return (t.isEmpty() || t.equalsIgnoreCase("null") || t.equalsIgnoreCase("undefined")) ? null : t;
    }

    private static String coalesce(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v;
    }

    private static String defaultIfBlank(String v, String def) {
        if (v == null) return def;
        String t = v.trim();
        return (t.isEmpty() || t.equalsIgnoreCase("null") || t.equalsIgnoreCase("undefined")) ? def : t;
    }

    /** 지번주소 생성: "서울특별시 광진구 중곡동 18-109" / 산일 경우 "산18-109" */
    private static String buildParcelAddress(String sido, String sgg, String umd,
                                             String mtnYn, String mno, String sno) {
        String bunji = ("1".equals(mtnYn) ? "산" : "") + mno + (isNonZero(sno) ? "-" + sno : "");
        String prefix = (umd == null || umd.isBlank())
                ? String.format("%s %s", sido.trim(), sgg.trim())
                : String.format("%s %s %s", sido.trim(), sgg.trim(), umd.trim());
        return String.format("%s %s", prefix, bunji).trim();
    }

    private static boolean isNonZero(String s) {
        if (s == null || s.isBlank()) return false;
        try { return Integer.parseInt(s) > 0; } catch (NumberFormatException e) { return true; }
    }

    private static double haversineMeters(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng/2) * Math.sin(dLng/2);
        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
