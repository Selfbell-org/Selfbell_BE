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

    private URI buildUriExactly(String type, String ctpvNm, String sggNm, String umdNm,
                                int pageNo, int numOfRows) {

        String ctpvEnc = URLEncoder.encode(ctpvNm, StandardCharsets.UTF_8);
        String sggEnc  = URLEncoder.encode(sggNm,  StandardCharsets.UTF_8);
        String umdEnc  = URLEncoder.encode(umdNm,  StandardCharsets.UTF_8);

        String url = BASE_URL
                .concat("?serviceKey=").concat(serviceKeyEncoded)
                .concat("&pageNo=").concat(String.valueOf(pageNo))
                .concat("&numOfRows=").concat(String.valueOf(numOfRows))
                .concat("&type=").concat(type)
                .concat("&ctpvNm=").concat(ctpvEnc)
                .concat("&sggNm=").concat(sggEnc)
                .concat("&umdNm=").concat(umdEnc);

        String safe = url.replaceAll("(serviceKey=)([^&]{8})[^&]*", "$1$2******");
        log.info("[CriminalAPI] 요청 URL: {}", safe);

        return URI.create(url);
    }

    /** XML → DTO */
    public CriminalApiXmlDto getCriminalDataXml(String ctpvNm, String sggNm, String umdNm) throws Exception {
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
        URI uri = buildUriExactly("json", ctpvNm, sggNm, umdNm, 1, 10);
        String json;
        try {
            json = restTemplate.getForObject(uri, String.class);
        } catch (RestClientResponseException e) {
            log.error("HTTP {} 에러. 응답 본문:\n{}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
        if (json == null || json.isBlank()) throw new IllegalStateException("API 응답이 비어있습니다.");
        return json;
    }

    /** API items → 지번주소 → VWorld 지오코딩 → 좌표 리스트 */
    public List<CriminalCoordDto> getCriminalCoords(String ctpvNm, String sggNm, String umdNm) throws Exception {
        CriminalApiXmlDto xmlDto = getCriminalDataXml(ctpvNm, sggNm, umdNm);
        List<CriminalApiXmlDto.Item> items = Optional.ofNullable(xmlDto.getBody())
                .map(CriminalApiXmlDto.Body::getItems)
                .map(CriminalApiXmlDto.Items::getItem)
                .orElseGet(List::of);

        return items.stream()
                .map(it -> {
                    String sido = coalesce(it.getCtpvNm(), ctpvNm);
                    String sgg  = coalesce(it.getSggNm(),  sggNm);
                    String umd  = coalesce(it.getUmdNm(),  umdNm);
                    String mtn  = defaultIfBlank(it.getMtnYn(), "0");
                    String mno  = defaultIfBlank(it.getMno(), "0");
                    String sno  = defaultIfBlank(it.getSno(), "0");

                    String address = buildParcelAddress(sido, sgg, umd, mtn, mno, sno);

                    return vworldClient.geocodeParcel(address)
                            .map(p -> CriminalCoordDto.builder()
                                    .address(address)
                                    .latitude(p.lat())
                                    .longitude(p.lng())
                                    .build())
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /* ---------- 유틸 ---------- */

    private static String coalesce(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v;
    }

    private static String defaultIfBlank(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }

    /** 지번주소 문자열 생성: "서울특별시 광진구 중곡동 18-109" / 산일 경우 "산18-109" */
    private static String buildParcelAddress(String sido, String sgg, String umd,
                                             String mtnYn, String mno, String sno) {
        String bunji = ("1".equals(mtnYn) ? "산" : "") + mno + (isNonZero(sno) ? "-" + sno : "");
        return String.format("%s %s %s %s", sido.trim(), sgg.trim(), umd.trim(), bunji);
    }

    private static boolean isNonZero(String s) {
        if (s == null || s.isBlank()) return false;
        try { return Integer.parseInt(s) > 0; } catch (NumberFormatException e) { return true; }
    }
}
