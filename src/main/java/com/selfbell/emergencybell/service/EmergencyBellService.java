package com.selfbell.emergencybell.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.selfbell.emergencybell.domain.EmergencyBell;
import com.selfbell.emergencybell.dto.EmergencyBellXmlDto;
import com.selfbell.emergencybell.repository.EmergencyBellRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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

        String xmlResponse = restTemplate.getForObject(url, String.class);

        if (xmlResponse == null || xmlResponse.trim().isEmpty()) {
            throw new RuntimeException("API 응답이 비어있습니다.");
        }

        xmlResponse = xmlResponse.replaceAll("<!DOCTYPE[^>]*>", "");

        XmlMapper xmlMapper = new XmlMapper();
        EmergencyBellXmlDto dto = xmlMapper.readValue(xmlResponse, EmergencyBellXmlDto.class);

        if (dto.getBody() == null) {
            System.err.println("파싱은 되었지만 body가 null 입니다.");
        } else {
            System.out.println("데이터 개수: " + dto.getBody().getTotalCount());
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
        if (items == null) {
            return;
        }

        for (EmergencyBellXmlDto.Item item : items) {
            EmergencyBell entity = EmergencyBell.builder()
                    .id(item.getOBJT_ID())
                    .facilityType(item.getFCLTY_TY())
                    .managingInstitution(item.getMNG_INST())
                    .installPurpose(item.getINS_PURPOS())
                    .installType(item.getINS_TYPE())
                    .installDetail(item.getINS_DETAIL())
                    .roadAddress(item.getRN_ADRES())
                    .lotNumberAddress(item.getADRES())
                    .latitude(item.getLAT())
                    .longitude(item.getLON())
                    .linkType(item.getLNK_TYPE())
                    .policeLinked(item.getFLAG_POL_L())
                    .securityLinked(item.getFLAG_SEC_L())
                    .managementLinked(item.getFLAG_MNG_L())
                    .addition(item.getADDITION())
                    .installYear(item.getINS_YEAR() != null ? item.getINS_YEAR().intValue() : null)
                    .lastInspectionResult(item.getLAST_INSPT())
                    .managementPhone(item.getMNG_TEL())
                    .serviceLinked(item.getFLAG_SERVI())
                    .provinceCode(item.getCTPRVN_CD() != null ? item.getCTPRVN_CD().intValue() : null)
                    .districtCode(item.getSGG_CD() != null ? item.getSGG_CD().intValue() : null)
                    .townshipCode(item.getEMD_CD() != null ? item.getEMD_CD().intValue() : null)
                    .coordX(item.getX())
                    .coordY(item.getY())
                    .dataType(item.getDATA_TY() != null ? item.getDATA_TY().intValue() : null)
                    .build();

            // LAST_INSPD String → LocalDate 변환 메서드 호출
            entity.setLastInspectionDateFromString(item.getLAST_INSPD());

            repository.save(entity);
        }
    }
}

