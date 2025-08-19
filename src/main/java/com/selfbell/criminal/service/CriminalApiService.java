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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CriminalApiService {

    @Value("${api.criminal.key}")
    private String serviceKeyEncoded;

    /** 공공데이터 페이지 사이즈(프로퍼티로 올려서 호출 수 감소) */
    @Value("${api.criminal.page-size:500}")
    private int apiPageSize;

    /** 결과 충분히 모이면 수확 종료 (너무 많이 지오코딩하지 않도록) */
    @Value("${nearby.top-results:200}")
    private int topNResults;

    /** 지오코딩 최대 제출 수 상한 */
    @Value("${nearby.max-geocode-attempts:1500}")
    private int maxGeocodeAttempts;

    private final RestTemplate restTemplate;
    private final VWorldClient vworldClient;

    /** 공유 스레드풀 주입 */
    @Qualifier("apiExecutor")     private final ExecutorService apiExecutor;
    @Qualifier("geocodeExecutor") private final ExecutorService geocodeExecutor;

    private static final String BASE_URL =
            "https://apis.data.go.kr/1383000/sais/SexualAbuseNoticeHouseNumAddrServiceV2/getSexualAbuseNoticeHouseNumAddrListV2";

    private static final double EARTH_RADIUS_M = 6371000.0;

    /* =========================
       공개 메서드
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
        Set<String> uniq = ConcurrentHashMap.newKeySet();

        CompletionService<CriminalCoordDto> cs = new ExecutorCompletionService<>(geocodeExecutor);
        int submitted = 0;

        for (var it : items) {
            String addr = buildParcelAddress(
                    coalesce(it.getCtpvNm(), ctpvNm),
                    coalesce(it.getSggNm(),  sggNm),
                    it.getUmdNm(),
                    defaultIfBlank(it.getMtnYn(), "0"),
                    defaultIfBlank(it.getMno(), "0"),
                    defaultIfBlank(it.getSno(), "0")
            );
            if (!uniq.add(addr)) continue;

            cs.submit(() -> vworldClient.geocodeParcel(addr)
                    .map(p -> CriminalCoordDto.builder()
                            .address(addr)
                            .lat(p.lat().doubleValue())
                            .lon(p.lon().doubleValue())
                            .distanceMeters(0.0)
                            .build())
                    .orElse(null));
            submitted++;
            if (submitted >= maxGeocodeAttempts) break;
        }

        List<CriminalCoordDto> out = new ArrayList<>();
        for (int i = 0; i < submitted; i++) {
            try {
                var r = cs.take().get();
                if (r != null) out.add(r);
            } catch (Exception ignore) {}
        }
        return out;
    }

    /** 위도/경도/반경을 받아 반경 내 결과(거리 포함) 반환 */
    public List<CriminalCoordDto> getNearbyCoords(double lat, double lon, int radiusMeters) throws Exception {
        int r = Math.max(100, Math.min(1000, radiusMeters));
        if (r != radiusMeters) {
            log.warn("[CriminalAPI] 반경 클램핑: 입력={}m → 사용={}m (허용: 100~1000m)", radiusMeters, r);
        }

        // 1) 중심 RAW + 내부/외부 링으로 UMD 후보 수집(경계 안전)
        var umdCandidates = discoverUmdsOnCircle(lat, lon, r);
        if (umdCandidates.isEmpty()) return List.of();

        // 2) 후보 UMD별 호출
        List<CriminalApiXmlDto.Item> items = new ArrayList<>();
        for (var region : umdCandidates) {
            items.addAll(fetchItemsOfUmd(region.ctpvNm(), region.sggNm(), region.umdNm()));
        }

        // 3) 지오코딩 + 반경 필터 + 정렬 (조기 종료 상한 적용)
        Set<String> uniqAddr = ConcurrentHashMap.newKeySet();
        CompletionService<CriminalCoordDto> cs = new ExecutorCompletionService<>(geocodeExecutor);

        int submitted = 0;
        for (var it : items) {
            String addr = buildParcelAddress(
                    it.getCtpvNm(), it.getSggNm(), it.getUmdNm(),
                    defaultIfBlank(it.getMtnYn(), "0"),
                    defaultIfBlank(it.getMno(), "0"),
                    defaultIfBlank(it.getSno(), "0")
            );
            if (!uniqAddr.add(addr)) continue;

            cs.submit(() -> vworldClient.geocodeParcel(addr).map(p -> {
                double d = haversineMeters(lat, lon, p.lat().doubleValue(), p.lon().doubleValue());
                if (d > r) return null;
                return CriminalCoordDto.builder()
                        .address(addr)
                        .lat(p.lat().doubleValue())
                        .lon(p.lon().doubleValue())
                        .distanceMeters(d)
                        .build();
            }).orElse(null));

            submitted++;
            if (submitted >= maxGeocodeAttempts) break;
        }

        List<CriminalCoordDto> buffer = new ArrayList<>(Math.min(submitted, topNResults));
        for (int i = 0; i < submitted; i++) {
            try {
                var res = cs.take().get();
                if (res != null) {
                    buffer.add(res);
                    if (buffer.size() >= topNResults) break; // 조기 수확 종료
                }
            } catch (Exception ignore) {}
        }

        buffer.sort(Comparator.comparingDouble(CriminalCoordDto::getDistanceMeters));
        if (buffer.size() > topNResults) {
            return buffer.subList(0, topNResults);
        }
        return buffer;
    }

    /* =========================
       공공데이터 호출
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
        try {
            String xml = restTemplate.getForObject(uri, String.class);
            if (xml == null || xml.isBlank()) throw new IllegalStateException("API 응답이 비어있습니다.");
            return parseXml(xml);
        } catch (RestClientResponseException e) {
            log.error("HTTP {} 에러. 응답 본문:\n{}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw e;
        }
    }

    public String getCriminalDataJson(String ctpvNm, String sggNm, String umdNm) throws Exception {
        URI uri = buildUriExactly("json", ctpvNm, sggNm, umdNm, 1, 10);
        try {
            String json = restTemplate.getForObject(uri, String.class);
            if (json == null || json.isBlank()) throw new IllegalStateException("API 응답이 비어있습니다.");
            return json;
        } catch (RestClientResponseException e) {
            log.error("HTTP {} 에러. 응답 본문:\n{}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw e;
        }
    }

    private List<CriminalApiXmlDto.Item> fetchAllItemsOfSgg(String ctpvNm, String sggNm) throws Exception {
        int pageNo = 1, expectedTotal = Integer.MAX_VALUE;
        List<CriminalApiXmlDto.Item> acc = new ArrayList<>();

        while (acc.size() < expectedTotal) {
            URI uri = buildUriExactly("xml", ctpvNm, sggNm, null, pageNo, apiPageSize);
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
            if (pageNo > 200) { log.warn("[CriminalAPI] Page limit reached ({}).", pageNo); break; }
        }
        return acc;
    }

    private List<CriminalApiXmlDto.Item> fetchItemsOfUmd(String ctpvNm, String sggNm, String umdNm) throws Exception {
        int pageNo = 1, expectedTotal = Integer.MAX_VALUE;
        List<CriminalApiXmlDto.Item> acc = new ArrayList<>();

        while (acc.size() < expectedTotal) {
            URI uri = buildUriExactly("xml", ctpvNm, sggNm, umdNm, pageNo, apiPageSize);
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
            if (pageNo > 120) { log.warn("[CriminalAPI] (UMD) Page limit reached ({}).", pageNo); break; }
        }
        return acc;
    }

    /* =========================
       UMD 후보 수집(중심 RAW + 내부/외부 링)
       ========================= */
    private List<VWorldClient.AdminRegion> discoverUmdsOnCircle(double lat, double lon, int radiusM) {
        final int outerBearings = 16;
        final int innerBearings = 8;
        final int epsilon = Math.min(Math.max(radiusM / 6, 100), 250);
        final double dOuter = radiusM + epsilon;
        final double dInner = Math.max(radiusM / 2.0, 150);

        Set<String> uniq = new LinkedHashSet<>();
        List<VWorldClient.AdminRegion> out = new ArrayList<>();

        // 중심은 캐시 미사용(정확)
        vworldClient.reverseGeocodeRaw(lat, lon).ifPresent(ar -> {
            String key = ar.ctpvNm() + "|" + ar.sggNm() + "|" + ar.umdNm();
            if (uniq.add(key)) out.add(ar);
        });

        for (int i = 0; i < innerBearings; i++) {
            double[] p = offset(lat, lon, dInner, Math.toRadians(360.0 * i / innerBearings));
            vworldClient.reverseGeocode(p[0], p[1]).ifPresent(ar -> {
                String key = ar.ctpvNm() + "|" + ar.sggNm() + "|" + ar.umdNm();
                if (uniq.add(key)) out.add(ar);
            });
        }
        for (int i = 0; i < outerBearings; i++) {
            double[] p = offset(lat, lon, dOuter, Math.toRadians(360.0 * i / outerBearings));
            vworldClient.reverseGeocode(p[0], p[1]).ifPresent(ar -> {
                String key = ar.ctpvNm() + "|" + ar.sggNm() + "|" + ar.umdNm();
                if (uniq.add(key)) out.add(ar);
            });
        }

        log.debug("[CriminalAPI] UMD 후보 수: {} (r={}, inner={}, outer={}, eps={})",
                out.size(), radiusM, dInner, dOuter, epsilon);
        return out;
    }

    /* =========================
       XML 파싱 (싱글턴)
       ========================= */
    private static final XmlMapper XML = XmlMapper.builder()
            .enable(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL)
            .build();

    static {
        XML.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        XML.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private CriminalApiXmlDto parseXml(String xmlRaw) throws Exception {
        String sanitized = xmlRaw.replace("\uFEFF", "")
                .replaceAll("(?s)<!DOCTYPE.*?>", "")
                .replaceAll("(?s)<!--.*?-->", "");
        CriminalApiXmlDto dto = XML.readValue(sanitized, CriminalApiXmlDto.class);

        if (dto.getHeader() == null || dto.getBody() == null)
            throw new IllegalStateException("XML 파싱 실패: header/body 누락");

        String code = String.valueOf(dto.getHeader().getResultCode());
        if (!code.equals("00") && !code.equals("0"))
            throw new IllegalStateException("공공데이터 API 오류: " + dto.getHeader().getResultMsg());

        return dto;
    }

    /* =========================
       유틸
       ========================= */
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

    private static String buildParcelAddress(String sido, String sgg, String umd,
                                             String mtnYn, String mno, String sno) {
        String bunji = ("1".equals(mtnYn) ? "산" : "") + mno + (isNonZero(sno) ? "-" + sno : "");
        String prefix = (umd == null || umd.isBlank())
                ? String.format("%s %s", safe(sido), safe(sgg))
                : String.format("%s %s %s", safe(sido), safe(sgg), safe(umd));
        return String.format("%s %s", prefix, bunji).trim();
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

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
}
