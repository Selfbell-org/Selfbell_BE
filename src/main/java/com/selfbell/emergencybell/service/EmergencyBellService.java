package com.selfbell.emergencybell.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.selfbell.emergencybell.domain.EmergencyBell;
import com.selfbell.emergencybell.dto.EmergencyBellSummaryDto;
import com.selfbell.emergencybell.dto.EmergencyBellXmlDto;
import com.selfbell.emergencybell.repository.EmergencyBellRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyBellService {

    @Value("${api.emergencybell.key}")
    private String serviceKey;

    // 증분 동기화 범위
    @Value("${emergencybell.incremental.pages:5}")
    private int incrementalPages;

    @Value("${emergencybell.incremental.num-of-rows:100}")
    private int incrementalNumOfRows;

    // 전체 동기화시 페이지당 건수
    @Value("${emergencybell.full-sync.num-of-rows:500}")
    private int fullSyncNumOfRows;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;              // 새로 주입 (XmlMapperConfig에서 등록)
    private final EmergencyBellRepository repository;
    private final JdbcTemplate jdbcTemplate;

    private static final String BASE_URL =
            "https://safemap.go.kr/openApiService/data/getCmmpoiEmgbellData.do";

    /** 외부 API 호출 (XML) — URL 인코딩/키 보존 + XML 정화 */
    public EmergencyBellXmlDto getEmergencyBellData(int pageNo, int numOfRows) throws Exception {
        URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("serviceKey", serviceKey)   // 이미 인코딩된 키도 build(true)로 안전 보존
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                .build(true)                            // pre-encoded 보호
                .toUri();

        log.info("API 호출 URL: {}", uri);

        String xmlResponse = restTemplate.getForObject(uri, String.class);
        if (xmlResponse == null || xmlResponse.trim().isEmpty()) {
            throw new RuntimeException("API 응답이 비어있습니다.");
        }

        // XML 깨짐 방어
        String safeXml = sanitizeXml(xmlResponse);

        EmergencyBellXmlDto dto = xmlMapper.readValue(safeXml, EmergencyBellXmlDto.class);
        if (dto.getBody() == null) {
            log.error("파싱 완료했지만 body가 null 입니다. (pageNo={}, numOfRows={})", pageNo, numOfRows);
        } else {
            log.info("pageNo={}, numOfRows={}, totalCount={}", pageNo, numOfRows, dto.getBody().getTotalCount());
        }
        return dto;
    }

    /** JSON 직렬화 (디버그/확인용) */
    public String getEmergencyBellDataAsJson(int pageNo, int numOfRows) throws Exception {
        EmergencyBellXmlDto dto = getEmergencyBellData(pageNo, numOfRows);
        return objectMapper.writeValueAsString(dto);
    }

    /** 9개 필드 요약 데이터 반환 */
    public List<EmergencyBellSummaryDto> getFilteredEmergencyBellData(int pageNo, int numOfRows) throws Exception {
        EmergencyBellXmlDto dto = getEmergencyBellData(pageNo, numOfRows);
        var items = (dto.getBody() != null && dto.getBody().getItems() != null)
                ? dto.getBody().getItems().getItem()
                : new ArrayList<EmergencyBellXmlDto.Item>();

        return items.stream()
                .filter(i -> i.getOBJT_ID() != null)
                .map(item -> EmergencyBellSummaryDto.builder()
                        .lon(item.getLON())
                        .lat(item.getLAT())
                        .insDetail(item.getINS_DETAIL())
                        .objtId(item.getOBJT_ID())
                        .mngTel(item.getMNG_TEL())
                        .adres(item.getADRES())
                        .insType(item.getINS_TYPE())
                        .x(item.getX())
                        .y(item.getY())
                        .build())
                .collect(Collectors.toList());
    }

    /** 반경 내 검색 */
    public List<EmergencyBellSummaryDto> findNearbyEmergencyBells(double userLat, double userLon, double radiusInMeters) {
        var raw = repository.findWithinRadiusRaw(userLat, userLon, radiusInMeters);
        return raw.stream().map(row -> {
            Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
            Double latitude = row[1] != null ? ((Number) row[1]).doubleValue() : null;
            Double longitude = row[2] != null ? ((Number) row[2]).doubleValue() : null;
            String installDetail = row[3] != null ? row[3].toString() : null;
            String managementPhone = row[4] != null ? row[4].toString() : null;
            String lotNumberAddress = row[5] != null ? row[5].toString() : null;
            String installType = row[6] != null ? row[6].toString() : null;
            Double coordX = row[7] != null ? ((Number) row[7]).doubleValue() : null;
            Double coordY = row[8] != null ? ((Number) row[8]).doubleValue() : null;
            Double distance = row[9] != null ? ((Number) row[9]).doubleValue() : null;

            return EmergencyBellSummaryDto.builder()
                    .objtId(id)
                    .lat(latitude)
                    .lon(longitude)
                    .insDetail(installDetail)
                    .mngTel(managementPhone)
                    .adres(lotNumberAddress)
                    .insType(installType)
                    .x(coordX)
                    .y(coordY)
                    .distance(distance)
                    .build();
        }).collect(Collectors.toList());
    }

    //각 비상벨 상세조회 기능
    @Transactional(readOnly = true)
    public Optional<EmergencyBellSummaryDto> getEmergencyBellDetail(Long id) {
        return repository.findById(id).map(e ->
                EmergencyBellSummaryDto.builder()
                        .objtId(e.getId())
                        .lat(e.getLat())
                        .lon(e.getLon())
                        .insDetail(e.getIns_DETAIL())
                        .mngTel(e.getMng_TEL())
                        .adres(e.getAdres())
                        .insType(e.getIns_TYPE())
                        .x(e.getX())
                        .y(e.getY())
                        .build()
        );
    }

    /** JPA 기반 Upsert (느리지만 간단) */
    @Transactional
    public void saveOrUpdateEmergencyBellsJpa(List<EmergencyBellXmlDto.Item> items) {
        if (items == null || items.isEmpty()) {
            log.warn("저장할 데이터가 없습니다.");
            return;
        }
        for (EmergencyBellXmlDto.Item item : items) {
            Long id = item.getOBJT_ID();
            if (id == null) {
                log.warn("Item에 OBJT_ID가 없어 건너뜁니다.");
                continue;
            }
            EmergencyBell entity = repository.findById(id).orElse(
                    EmergencyBell.builder().id(id).build()
            );
            entity.updateFromItem(
                    id,
                    item.getLAT(),
                    item.getLON(),
                    item.getINS_DETAIL(),
                    item.getMNG_TEL(),
                    item.getADRES(),
                    item.getINS_TYPE(),
                    item.getX(),
                    item.getY()
            );
            repository.save(entity);
        }
        log.info("JPA 기반 Upsert 완료. 처리 건수: {}", items.size());
    }

    /** =========================
     *  JDBC Batch Upsert (권장)
     *  ========================= */
    @Transactional
    public void bulkUpsertEmergencyBells(List<EmergencyBellXmlDto.Item> items) {
        if (items == null || items.isEmpty()) {
            log.warn("저장할 데이터가 없습니다.");
            return;
        }

        final String sql = "INSERT INTO emergency_bell " +
                "(id, lon, lat, ins_DETAIL, mng_TEL, adres, ins_TYPE, x, y) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "lon = VALUES(lon), " +
                "lat = VALUES(lat), " +
                "ins_DETAIL = VALUES(ins_DETAIL), " +
                "mng_TEL = VALUES(mng_TEL), " +
                "adres = VALUES(adres), " +
                "ins_TYPE = VALUES(ins_TYPE), " +
                "x = VALUES(x), " +
                "y = VALUES(y)";

        int batchSize = 1000;
        for (int start = 0; start < items.size(); start += batchSize) {
            int end = Math.min(start + batchSize, items.size());
            List<EmergencyBellXmlDto.Item> slice = items.subList(start, end);

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    EmergencyBellXmlDto.Item it = slice.get(i);

                    setNullableLong(ps, 1, it.getOBJT_ID());
                    setNullableDouble(ps, 2, it.getLON());
                    setNullableDouble(ps, 3, it.getLAT());
                    setNullableString(ps, 4, it.getINS_DETAIL());
                    setNullableString(ps, 5, it.getMNG_TEL());
                    setNullableString(ps, 6, it.getADRES());
                    setNullableString(ps, 7, it.getINS_TYPE());
                    setNullableDouble(ps, 8, it.getX());
                    setNullableDouble(ps, 9, it.getY());
                }

                @Override
                public int getBatchSize() {
                    return slice.size();
                }
            });
            log.info("Batch upsert 처리 - {} ~ {} (총 {}건)", start + 1, end, slice.size());
        }
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) ps.setNull(index, Types.VARCHAR);
        else ps.setString(index, value);
    }

    private void setNullableDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value == null) ps.setNull(index, Types.DOUBLE);
        else ps.setDouble(index, value);
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) ps.setNull(index, Types.BIGINT);
        else ps.setLong(index, value);
    }

    /** 전체 페이지 순회 + JDBC Batch Upsert (안정화) */
    public void fullSyncBulk() {
        try {
            // 1) 첫 페이지로 totalCount 확보
            EmergencyBellXmlDto first = getEmergencyBellData(1, fullSyncNumOfRows);
            int total = (first.getBody() != null) ? first.getBody().getTotalCount() : 0;
            if (total <= 0) {
                log.warn("totalCount가 0입니다. 중단합니다.");
                return;
            }
            int pages = (int) Math.ceil((double) total / fullSyncNumOfRows);
            log.info("전체 동기화 시작: totalCount={}, pages={}, numOfRows={}", total, pages, fullSyncNumOfRows);

            // 2) 1페이지 처리
            List<EmergencyBellXmlDto.Item> items1 = (first.getBody() != null && first.getBody().getItems() != null)
                    ? first.getBody().getItems().getItem()
                    : new ArrayList<>();
            bulkUpsertEmergencyBells(items1);

            // 3) 2..N 페이지 고정 루프 (페이지 단위 실패는 continue)
            for (int p = 2; p <= pages; p++) {
                try {
                    EmergencyBellXmlDto dto = getEmergencyBellData(p, fullSyncNumOfRows);
                    List<EmergencyBellXmlDto.Item> items =
                            (dto.getBody() != null && dto.getBody().getItems() != null)
                                    ? dto.getBody().getItems().getItem()
                                    : new ArrayList<>();

                    bulkUpsertEmergencyBells(items);
                    log.info("전체 동기화 진행: page={}/{}", p, pages);

                    // 속도 제한 방지
                    Thread.sleep(120L);
                } catch (Exception pageEx) {
                    log.error("페이지 {} 처리 중 오류. 건너뜀: {}", p, pageEx.getMessage(), pageEx);
                }
            }

            log.info("전체 동기화 완료 (JDBC batch upsert)");

        } catch (Exception e) {
            log.error("전체 동기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("전체 동기화 실패: " + e.getMessage(), e);
        }
    }

    /** 증분 동기화: 최신 N페이지만 조회하여 upsert */
    public void incrementalSync() {
        for (int page = 1; page <= incrementalPages; page++) {
            try {
                EmergencyBellXmlDto dto = getEmergencyBellData(page, incrementalNumOfRows);
                var body = dto.getBody();
                var items = (body != null && body.getItems() != null)
                        ? body.getItems().getItem()
                        : new ArrayList<EmergencyBellXmlDto.Item>();
                bulkUpsertEmergencyBells(items);
                log.info("증분 동기화 완료 - page {}", page);

                Thread.sleep(80L);
            } catch (Exception e) {
                log.error("증분 동기화 페이지 {} 처리 실패: {}", page, e.getMessage(), e);
            }
        }
        log.info("증분 동기화 전체 완료 (pages={}, rows/page={})", incrementalPages, incrementalNumOfRows);
    }

    // ===== XML 정리 유틸 =====
    private String sanitizeXml(String xml) {
        if (xml == null) return "";
        String s = xml
                // DOCTYPE 제거
                .replaceAll("(?is)<!DOCTYPE[^>]*>", "")
                // 표준 엔티티/문자 참조를 제외한 잘못된 '&' 치환
                .replaceAll("&(?!amp;|lt;|gt;|quot;|apos;|#[0-9]+;|#x[0-9a-fA-F]+;)", "&amp;");
        // BOM/컨트롤문자 방지
        s = new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        return s;
    }
}
