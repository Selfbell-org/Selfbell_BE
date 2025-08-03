package com.selfbell.emergencybell.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.selfbell.emergencybell.dto.EmergencyBellXmlDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class EmergencyBellService {

    @Value("${api.emergencybell.key}")
    private String serviceKey;

    private final RestTemplate restTemplate;

    public EmergencyBellXmlDto getEmergencyBellData(int pageNo, int numOfRows) throws Exception {
        String url = "https://safemap.go.kr/openApiService/data/getCmmpoiEmgbellData.do" +
                "?serviceKey=" + serviceKey +
                "&pageNo=" + pageNo +
                "&numOfRows=" + numOfRows;

        String xmlResponse = restTemplate.getForObject(url, String.class);

        System.out.println("==== API XML Raw Response ====");
        System.out.println(xmlResponse);

        if (xmlResponse == null || xmlResponse.trim().isEmpty()) {
            throw new RuntimeException("API 응답이 비어있습니다.");
        }

        xmlResponse = xmlResponse.replaceAll("<!DOCTYPE[^>]*>", "");

        XmlMapper xmlMapper = new XmlMapper();
        EmergencyBellXmlDto dto = xmlMapper.readValue(xmlResponse, EmergencyBellXmlDto.class);

        if (dto.getBody() == null) {
            System.err.println("⚠️ 파싱은 되었지만 body가 null 입니다.");
        } else {
            System.out.println("✅ 데이터 개수: " + dto.getBody().getTotalCount());
        }

        return dto;
    }

    public String getEmergencyBellDataAsJson(int pageNo, int numOfRows) throws Exception {
        EmergencyBellXmlDto dto = getEmergencyBellData(pageNo, numOfRows);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(dto);
    }
}
