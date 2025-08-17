package com.selfbell.criminal.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.selfbell.criminal.dto.CriminalApiXmlDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CriminalApiService {

    @Value("${api.criminal.key}")
    private String serviceKeyEncoded;

    private final RestTemplate restTemplate;

    private static final String BASE_URL =
            "https://apis.data.go.kr/1383000/sais/SexualAbuseNoticeHouseNumAddrServiceV2/getSexualAbuseNoticeHouseNumAddrListV2";

    private URI buildUriExactly(String type, String ctpvNm, String sggNm, String umdNm,
                                int pageNo, int numOfRows) {

        // 한글 파라미터만 1회 인코딩
        String ctpvEnc = URLEncoder.encode(ctpvNm, StandardCharsets.UTF_8);
        String sggEnc  = URLEncoder.encode(sggNm,  StandardCharsets.UTF_8);
        String umdEnc  = URLEncoder.encode(umdNm,  StandardCharsets.UTF_8);

        // serviceKey는 yml에 넣은 인코딩된 문자열을 그대로 사용 (절대 재인코딩 X)
        String url = BASE_URL
                + "?serviceKey=" + serviceKeyEncoded
                + "&pageNo=" + pageNo
                + "&numOfRows=" + numOfRows
                + "&type=" + type      // "json" 또는 "xml" 그대로
                + "&ctpvNm=" + ctpvEnc
                + "&sggNm="  + sggEnc
                + "&umdNm="  + umdEnc;

        // 로그 마스킹
        String safe = url.replaceAll("(serviceKey=)([^&]{8})[^&]*", "$1$2******");
        log.info("[CriminalAPI] 요청 URL: {}", safe);

        return URI.create(url); // 추가 인코딩 없음
    }

    /** XML → DTO */
    public CriminalApiXmlDto getCriminalDataXml(String ctpvNm, String sggNm, String umdNm) throws Exception {
        // 요청 예시와 동일하게 pageNo=1, numOfRows=10, type=xml
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

    /** JSON 그대로 반환 */
    public String getCriminalDataJson(String ctpvNm, String sggNm, String umdNm) throws Exception {
        // 요청 예시와 동일하게 pageNo=1, numOfRows=10, type=json
        URI uri = buildUriExactly("json", ctpvNm, sggNm, umdNm, 1, 10);

        String json;
        try {
            var headers = new org.springframework.http.HttpEntity<>(new HttpHeaders() {{
                set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            }});
            json = restTemplate.getForObject(uri, String.class);
        } catch (RestClientResponseException e) {
            log.error("HTTP {} 에러. 응답 본문:\n{}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw e;
        }

        if (json == null || json.isBlank()) throw new IllegalStateException("API 응답이 비어있습니다.");
        return json;
    }
}
