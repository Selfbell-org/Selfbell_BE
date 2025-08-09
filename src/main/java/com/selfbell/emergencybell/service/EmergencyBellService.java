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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyBellService {

    @Value("${api.emergencybell.key}")
    private String serviceKey;

    private final RestTemplate restTemplate;
    private final EmergencyBellRepository repository;

    public EmergencyBellXmlDto getEmergencyBellData(int pageNo, int numOfRows) throws Exception {
        String url = "https://safemap.go.kr/openApiService/data/getCmmpoiEmgbellData.do" +
                "?serviceKey=" + serviceKey +
                "&pageNo=" + pageNo +
                "&numOfRows=" + numOfRows;

        log.info("API 호출 URL: {}", url);

        String xmlResponse = restTemplate.getForObject(url, String.class);

        if (xmlResponse == null || xmlResponse.trim().isEmpty()) {
            throw new RuntimeException("API 응답이 비어있습니다.");
        }

        xmlResponse = xmlResponse.replaceAll("<!DOCTYPE[^>]*>", "");

        XmlMapper xmlMapper = new XmlMapper();
        EmergencyBellXmlDto dto = xmlMapper.readValue(xmlResponse, EmergencyBellXmlDto.class);

        if (dto.getBody() == null) {
            log.error("파싱은 되었지만 body가 null 입니다.");
        } else {
            log.info("데이터 개수: {}", dto.getBody().getTotalCount());
        }

        return dto;
    }

    public String getEmergencyBellDataAsJson(int pageNo, int numOfRows) throws Exception {
        EmergencyBellXmlDto dto = getEmergencyBellData(pageNo, numOfRows);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(dto);
    }

    @Transactional
    public void saveOrUpdateEmergencyBells(List<EmergencyBellXmlDto.Item> items) {
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
                    EmergencyBell.builder()
                            .id(id)
                            .build()
            );

            // 오직 9개 필드만 업데이트
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
            log.debug("Upsert(요약) 완료: ID = {}", id);
        }

        log.info("전체 데이터 Upsert(요약 9개 필드) 완료. 처리 건수: {}", items.size());
    }

    public List<EmergencyBellSummaryDto> getFilteredEmergencyBellData(int pageNo, int numOfRows) throws Exception {
        EmergencyBellXmlDto dto = getEmergencyBellData(pageNo, numOfRows);

        var items = dto.getBody().getItems().getItem();

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

    public List<EmergencyBellSummaryDto> findNearbyEmergencyBells(double userLat, double userLon, double radiusInMeters) {
        var raw = repository.findWithinRadiusRaw(userLat, userLon, radiusInMeters);

        return raw.stream().map(row -> {
            // row 순서: 0:id,1:latitude,2:longitude,3:install_detail,4:management_phone,5:lot_number_address,6:install_type,7:coord_x,8:coord_y,9:distance
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
}
