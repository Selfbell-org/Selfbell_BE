package com.selfbell.emergencybell.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.selfbell.emergencybell.domain.EmergencyBell;
import com.selfbell.emergencybell.dto.EmergencyBellXmlDto;
import com.selfbell.emergencybell.repository.EmergencyBellRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

            EmergencyBell entity = repository.findById(id).orElse(
                    EmergencyBell.builder()
                            .id(id)
                            .build()
            );

            // 각 필드별 업데이트 메서드 호출
            entity.updateFacilityType(item.getFCLTY_TY());
            entity.updateManagingInstitution(item.getMNG_INST());
            entity.updateInstallPurpose(item.getINS_PURPOS());
            entity.updateInstallType(item.getINS_TYPE());
            entity.updateInstallDetail(item.getINS_DETAIL());
            entity.updateRoadAddress(item.getRN_ADRES());
            entity.updateLotNumberAddress(item.getADRES());
            entity.updateLatitude(item.getLAT());
            entity.updateLongitude(item.getLON());
            entity.updateLinkType(item.getLNK_TYPE());
            entity.updatePoliceLinked(item.getFLAG_POL_L());
            entity.updateSecurityLinked(item.getFLAG_SEC_L());
            entity.updateManagementLinked(item.getFLAG_MNG_L());
            entity.updateAddition(item.getADDITION());
            entity.updateInstallYear(item.getINS_YEAR() != null ? item.getINS_YEAR().intValue() : null);
            entity.updateLastInspectionResult(item.getLAST_INSPT());
            entity.updateManagementPhone(item.getMNG_TEL());
            entity.updateServiceLinked(item.getFLAG_SERVI());
            entity.updateProvinceCode(item.getCTPRVN_CD() != null ? item.getCTPRVN_CD().intValue() : null);
            entity.updateDistrictCode(item.getSGG_CD() != null ? item.getSGG_CD().intValue() : null);
            entity.updateTownshipCode(item.getEMD_CD() != null ? item.getEMD_CD().intValue() : null);
            entity.updateCoordX(item.getX());
            entity.updateCoordY(item.getY());
            entity.updateDataType(item.getDATA_TY() != null ? item.getDATA_TY().intValue() : null);

            entity.updateLastInspectionDateFromString(item.getLAST_INSPD());

            repository.save(entity);
            log.debug("Upsert 완료: ID = {}", id);
        }

        log.info("전체 데이터 Upsert 완료. 처리 건수: {}", items.size());
    }

    public List<Map<String, Object>> getFilteredEmergencyBellData(int pageNo, int numOfRows) throws Exception {
        EmergencyBellXmlDto dto = getEmergencyBellData(pageNo, numOfRows);

        List<EmergencyBellXmlDto.Item> items = dto.getBody().getItems().getItem();

        // 필요한 필드만 Map으로 추출
        return items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("latitude", item.getLAT()); //위도
            map.put("longitude", item.getLON()); //경도
            map.put("ins_DETAIL", item.getINS_DETAIL()); //ex)인왕산도시자연공원(청운지구 서시정)
            map.put("address", item.getADRES()); //주소 ex)서울특별시 종로구 청운동 7-3
            map.put("ins_type", item.getINS_TYPE()); // ex) 화장실
            map.put("objt_ID", item.getOBJT_ID()); //ID 값
            map.put("x", item.getX()); //x좌표
            map.put("y", item.getY()); //y좌표
            map.put("tel", item.getMNG_TEL()); //전화번호
            return map;
        }).collect(Collectors.toList());
    }

    // 반경 내 안심벨 조회 메서드
    public List<EmergencyBell> findNearbyEmergencyBells(double userLat, double userLon, double radiusInMeters) {
        return repository.findWithinRadius(userLat, userLon, radiusInMeters);
    }
}
