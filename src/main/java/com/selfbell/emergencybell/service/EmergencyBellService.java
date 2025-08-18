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

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyBellService {

    @Value("${api.emergencybell.key}")
    private String serviceKey;

    @Value("${emergencybell.incremental.pages:5}")
    private int incrementalPages;

    @Value("${emergencybell.incremental.num-of-rows:100}")
    private int incrementalNumOfRows;

    @Value("${emergencybell.full-sync.num-of-rows:500}")
    private int fullSyncNumOfRows;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;
    private final EmergencyBellRepository repository;
    private final JdbcTemplate jdbcTemplate;

    private static final String BASE_URL =
            "https://safemap.go.kr/openApiService/data/getCmmpoiEmgbellData.do";

    /** 외부 API 호출 (XML) — URL 인코딩/키 보존 + XML 정화 */
    public EmergencyBellXmlDto getEmergencyBellData(int pageNo, int numOfRows) throws Exception {
        URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                .build(true)
                .toUri();

        log.info("API 호출 URL: {}", uri);

        String xmlResponse = restTemplate.getForObject(uri, String.class);
        if (xmlResponse == null || xmlResponse.trim().isEmpty()) {
            throw new RuntimeException("API 응답이 비어있습니다.");
        }

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
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 반경 내 검색
     * - 기존 Repository 결과를 사용하되,
     * - DTO에는 objt_ID, lat, lon, ins_DETAIL, distance만 채움(나머지는 null → JSON 미포함)
     */
    public List<EmergencyBellSummaryDto> findNearbyEmergencyBells(double userLat, double userLon, double radiusInMeters) {
        var raw = repository.findWithinRadiusRaw(userLat, userLon, radiusInMeters);
        // 인덱스: 0=id, 1=lat, 2=lon, 3=insDetail, 4=mngTel, 5=adres, 6=insType, 7=distance
        return raw.stream().map(row -> {
            Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
            BigDecimal latitude = row[1] != null ? new BigDecimal(row[1].toString()) : null;
            BigDecimal longitude = row[2] != null ? new BigDecimal(row[2].toString()) : null;
            String installDetail = row[3] != null ? row[3].toString() : null;
            Double distance = row[7] != null ? ((Number) row[7]).doubleValue() : null;

            return EmergencyBellSummaryDto.builder()
                    .objtId(id)
                    .lat(latitude)
                    .lon(longitude)
                    .insDetail(installDetail)
                    .distance(distance)
                    // mngTel/addr/insType은 설정하지 않음 → null → 응답에서 제외
                    .build();
        }).collect(Collectors.toList());
    }

    // 상세조회: 풀 정보
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
                        .build()
        );
    }

    /** JPA 기반 Upsert */
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
                    item.getINS_TYPE()
            );
            repository.save(entity);
        }
        log.info("JPA 기반 Upsert 완료. 처리 건수: {}", items.size());
    }

    /** =========================
     *  JDBC Batch Upsert (x,y 제거)
     *  ========================= */
    @Transactional
    public void bulkUpsertEmergencyBells(List<EmergencyBellXmlDto.Item> items) {
        if (items == null || items.isEmpty()) {
            log.warn("저장할 데이터가 없습니다.");
            return;
        }

        final String sql = "INSERT INTO emergency_bell " +
                "(id, lon, lat, ins_DETAIL, mng_TEL, adres, ins_TYPE) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "lon = VALUES(lon), " +
                "lat = VALUES(lat), " +
                "ins_DETAIL = VALUES(ins_DETAIL), " +
                "mng_TEL = VALUES(mng_TEL), " +
                "adres = VALUES(adres), " +
                "ins_TYPE = VALUES(ins_TYPE)";

        int batchSize = 1000;
        for (int start = 0; start < items.size(); start += batchSize) {
            int end = Math.min(start + batchSize, items.size());
            List<EmergencyBellXmlDto.Item> slice = items.subList(start, end);

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    EmergencyBellXmlDto.Item it = slice.get(i);

                    setNullableLong(ps, 1, it.getOBJT_ID());
                    setNullableBigDecimal(ps, 2, it.getLON());
                    setNullableBigDecimal(ps, 3, it.getLAT());
                    setNullableString(ps, 4, it.getINS_DETAIL());
                    setNullableString(ps, 5, it.getMNG_TEL());
                    setNullableString(ps, 6, it.getADRES());
                    setNullableString(ps, 7, it.getINS_TYPE());
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

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) ps.setNull(index, Types.BIGINT);
        else ps.setLong(index, value);
    }

    private void setNullableBigDecimal(PreparedStatement ps, int index, BigDecimal value) throws SQLException {
        if (value == null) ps.setNull(index, Types.DECIMAL);
        else ps.setBigDecimal(index, value);
    }

    /** 전체 페이지 순회 + JDBC Batch Upsert */
    public void fullSyncBulk() {
        try {
            EmergencyBellXmlDto first = getEmergencyBellData(1, fullSyncNumOfRows);
            int total = (first.getBody() != null) ? first.getBody().getTotalCount() : 0;
            if (total <= 0) {
                log.warn("totalCount가 0입니다. 중단합니다.");
                return;
            }
            int pages = (int) Math.ceil((double) total / fullSyncNumOfRows);
            log.info("전체 동기화 시작: totalCount={}, pages={}, numOfRows={}", total, pages, fullSyncNumOfRows);

            List<EmergencyBellXmlDto.Item> items1 = (first.getBody() != null && first.getBody().getItems() != null)
                    ? first.getBody().getItems().getItem()
                    : new ArrayList<>();
            bulkUpsertEmergencyBells(items1);

            for (int p = 2; p <= pages; p++) {
                try {
                    EmergencyBellXmlDto dto = getEmergencyBellData(p, fullSyncNumOfRows);
                    List<EmergencyBellXmlDto.Item> items =
                            (dto.getBody() != null && dto.getBody().getItems() != null)
                                    ? dto.getBody().getItems().getItem()
                                    : new ArrayList<>();

                    bulkUpsertEmergencyBells(items);
                    log.info("전체 동기화 진행: page={}/{}", p, pages);
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
                .replaceAll("(?is)<!DOCTYPE[^>]*>", "")
                .replaceAll("&(?!amp;|lt;|gt;|quot;|apos;|#[0-9]+;|#x[0-9a-fA-F]+;)", "&amp;");
        s = new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        return s;
    }
}
