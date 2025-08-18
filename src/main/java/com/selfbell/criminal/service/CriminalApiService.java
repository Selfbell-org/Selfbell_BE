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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CriminalApiService {

    @Value("${api.criminal.key}")
    private String serviceKeyEncoded;

    private final RestTemplate restTemplate;
    private final VWorldClient vworldClient;

    private static final String BASE_URL =
            "https://apis.data.go.kr/1383000/sais/SexualAbuseNoticeHouseNumAddrServiceV2/getSexualAbuseNoticeHouseNumAddrListV2";

    private static final double EARTH_RADIUS_M = 6371000.0;

    /* =========================
       공개 메서드 (시·구만 받는 오버로드)
       ========================= */
    public CriminalApiXmlDto getCriminalDataXml(String ctpvNm, String sggNm) throws Exception {
        return getCriminalDataXml(ctpvNm, sggNm, null);
    }

    public String getCriminalDataJson(String ctpvNm, String sggNm) throws Exception {
        return getCriminalDataJson(ctpvNm, sggNm, null);
    }

    /** 시·구 전체를 지오코딩하여 좌표만 반환 (기존 유지) */
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
                    if (!uniq.add(addr)) return null;

                    return vworldClient.geocodeParcel(addr)
                            .map(p -> CriminalCoordDto.builder()
                                    .address(addr)
                                    .lat(p.lat())
                                    .lon(p.lon())
                                    .distanceMeters(0L)
                                    .build())
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** 위도/경도/반경을 받아 반경 내 결과(거리 포함)를 반환 — 경계 안전 + 호출 최소화 버전 */
    public List<CriminalCoordDto> getNearbyCoords(double lat, double lon, int radiusMeters) throws Exception {
        // 반경 제한 [100, 1000]m로 클램핑
        int r = Math.max(100, Math.min(1000, radiusMeters));
        if (r != radiusMeters) {
            log.warn("[CriminalAPI] 반경 클램핑: 입력={}m → 사용={}m (허용: 100~1000m)", radiusMeters, r);
        }

        // 1) 원 둘레 샘플링 + 중심점 역지오코딩 → (시/구/동) 후보 수집 (경계 안전)
        var umdCandidates = discoverUmdsOnCircle(lat, lon, r);
        if (umdCandidates.isEmpty()) return List.of();

        // 2) 후보 (시/구/동)마다만 호출 → 과다 호출 회피
        List<CriminalApiXmlDto.Item> items = new ArrayList<>();
        for (var region : umdCandidates) {
            items.addAll(fetchItemsOfUmd(region.ctpvNm(), region.sggNm(), region.umdNm()));
        }

        // 3) 주소 중복 제거 → 병렬 지오코딩(동시수 제한) → 반경 필터 → 거리순 정렬
        Set<String> uniq = new LinkedHashSet<>();
        return geocodeAndFilter(items, lat, lon, r, uniq);
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
        return parseXml(xml);
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
       ‘구 전체(모든 동)’ 페이지네이션 수집 (기존 유지)
       ========================= */
    private List<CriminalApiXmlDto.Item> fetchAllItemsOfSgg(String ctpvNm, String sggNm) throws Exception {
        final int PAGE_SIZE = 100;
        int pageNo = 1;
        int expectedTotal = Integer.MAX_VALUE;
        List<CriminalApiXmlDto.Item> acc = new ArrayList<>();

        while (acc.size() < expectedTotal) {
            URI uri = buildUriExactly("xml", ctpvNm, sggNm, null, pageNo, PAGE_SIZE);

            String xml = restTemplate.getForObject(uri, String.class);
            if (xml == null || xml.isBlank())
                throw new IllegalStateException("API 응답이 비어있습니다. page=" + pageNo);

            CriminalApiXmlDto dto = parseXml(xml);
            var body = dto.getBody();
            expectedTotal = Optional.ofNullable(body.getTotalCount()).orElse(0);
            var list = Optional.ofNullable(body.getItems())
                    .map(CriminalApiXmlDto.Items::getItem)
                    .orElseGet(List::of);

            acc.addAll(list);

            if (list.isEmpty()) break;
            pageNo++;
            if (pageNo > 200) {
                log.warn("[CriminalAPI] Page limit reached ({}). Stop fetching.", pageNo);
                break;
            }
        }
        return acc;
    }

    /* =========================
       새로 추가: UMD(동) 단위 페이지네이션 수집
       ========================= */
    private List<CriminalApiXmlDto.Item> fetchItemsOfUmd(String ctpvNm, String sggNm, String umdNm) throws Exception {
        final int PAGE_SIZE = 100; // 필요 시 200~500로 조정
        int pageNo = 1;
        int expectedTotal = Integer.MAX_VALUE;
        List<CriminalApiXmlDto.Item> acc = new ArrayList<>();

        while (acc.size() < expectedTotal) {
            URI uri = buildUriExactly("xml", ctpvNm, sggNm, umdNm, pageNo, PAGE_SIZE);

            String xml = restTemplate.getForObject(uri, String.class);
            if (xml == null || xml.isBlank())
                throw new IllegalStateException("API 응답이 비어있습니다. (UMD) page=" + pageNo);

            CriminalApiXmlDto dto = parseXml(xml);
            var body = dto.getBody();
            expectedTotal = Optional.ofNullable(body.getTotalCount()).orElse(0);
            var list = Optional.ofNullable(body.getItems())
                    .map(CriminalApiXmlDto.Items::getItem)
                    .orElseGet(List::of);

            acc.addAll(list);

            if (list.isEmpty()) break;
            pageNo++;
            if (pageNo > 100) { // 안전 한도
                log.warn("[CriminalAPI] (UMD) Page limit reached ({}). Stop fetching.", pageNo);
                break;
            }
        }
        return acc;
    }

    /* =========================
       새로 추가: 원 둘레 샘플링 기반 UMD 후보 수집 (경계 안전)
       ========================= */
    private List<VWorldClient.AdminRegion> discoverUmdsOnCircle(double lat, double lon, int radiusM) {
        final int bearings = 16; // 반경<=1km 가정: 16개 방위각 샘플
        final int epsilon = Math.min(Math.max(radiusM / 6, 100), 250); // r/6 ~ 250m
        final double d = (radiusM + epsilon);

        Set<String> uniq = new LinkedHashSet<>();
        List<VWorldClient.AdminRegion> out = new ArrayList<>();

        // 중심점 포함 (중심이 속한 동 보장)
        vworldClient.reverseGeocode(lat, lon).ifPresent(ar -> {
            String key = ar.ctpvNm() + "|" + ar.sggNm() + "|" + ar.umdNm();
            if (uniq.add(key)) out.add(ar);
        });

        // 원 둘레 샘플링
        for (int i = 0; i < bearings; i++) {
            double bearingDeg = (360.0 / bearings) * i;
            double[] p = offset(lat, lon, d, Math.toRadians(bearingDeg));
            vworldClient.reverseGeocode(p[0], p[1]).ifPresent(ar -> {
                String key = ar.ctpvNm() + "|" + ar.sggNm() + "|" + ar.umdNm();
                if (uniq.add(key)) out.add(ar);
            });
        }
        log.debug("[CriminalAPI] UMD 후보 수: {} ({}m, eps={}m)", out.size(), radiusM, epsilon);
        return out;
    }

    // 구면 오프셋(거리 d[m], 방위각 θ[rad])로 목적지 좌표 계산
    private static double[] offset(double latDeg, double lonDeg, double dMeters, double bearingRad) {
        double φ1 = Math.toRadians(latDeg);
        double λ1 = Math.toRadians(lonDeg);
        double δ  = dMeters / EARTH_RADIUS_M;

        double sinφ1 = Math.sin(φ1), cosφ1 = Math.cos(φ1);
        double sinδ  = Math.sin(δ),  cosδ  = Math.cos(δ);
        double sinθ  = Math.sin(bearingRad), cosθ = Math.cos(bearingRad);

        double sinφ2 = sinφ1 * cosδ + cosφ1 * sinδ * cosθ;
        double φ2 = Math.asin(sinφ2);
        double y  = sinθ * sinδ * cosφ1;
        double x  = cosδ - sinφ1 * sinφ2;
        double λ2 = λ1 + Math.atan2(y, x);

        return new double[]{ Math.toDegrees(φ2), Math.toDegrees(λ2) };
    }

    /* =========================
       새로 추가: 병렬 지오코딩 + 반경 필터 + 정렬
       ========================= */
    private List<CriminalCoordDto> geocodeAndFilter(
            List<CriminalApiXmlDto.Item> items, double lat, double lon, int radiusM, Set<String> uniq) {

        if (items.isEmpty()) return List.of();

        // 동시 지오코딩 제한 (쿼터/속도 균형)
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            var futures = items.stream()
                    .map(it -> pool.submit(() -> {
                        String addr = buildParcelAddress(
                                it.getCtpvNm(), it.getSggNm(), it.getUmdNm(),
                                defaultIfBlank(it.getMtnYn(), "0"),
                                defaultIfBlank(it.getMno(), "0"),
                                defaultIfBlank(it.getSno(), "0")
                        );
                        if (!uniq.add(addr)) return null;

                        return vworldClient.geocodeParcel(addr).map(p -> {
                            double d = haversineMeters(lat, lon, p.lat().doubleValue(), p.lon().doubleValue());
                            if (d > radiusM) return null;
                            return CriminalCoordDto.builder()
                                    .address(addr)
                                    .lat(p.lat())
                                    .lon(p.lon())
                                    .distanceMeters(Math.round(d))
                                    .build();
                        }).orElse(null);
                    }))
                    .toList();

            List<CriminalCoordDto> out = new ArrayList<>(futures.size());
            for (var f : futures) {
                try {
                    var r = f.get(); // RestTemplate 타임아웃이 걸려 있으므로 별도 타임아웃 없이도 안전
                    if (r != null) out.add(r);
                } catch (Exception e) {
                    // 개별 주소 실패는 전체 실패로 보지 않음
                }
            }
            out.sort(Comparator.comparingLong(CriminalCoordDto::getDistanceMeters));
            return out;
        } finally {
            pool.shutdown();
        }
    }

    /* =========================
       공통: XML 파싱 & 유틸
       ========================= */
    private CriminalApiXmlDto parseXml(String xmlRaw) throws Exception {
        String sanitized = xmlRaw.replace("\uFEFF", "")
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

    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
