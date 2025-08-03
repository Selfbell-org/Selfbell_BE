package com.selfbell.emergencybell.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@JacksonXmlRootElement(localName = "response") // XML 루트 태그 매핑
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@NoArgsConstructor
public class EmergencyBellXmlDto {

    private Header header;
    private Body body;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @NoArgsConstructor
    public static class Header {
        @JacksonXmlProperty(localName = "resultCode")
        private String resultCode;

        @JacksonXmlProperty(localName = "resultMsg")
        private String resultMsg;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @NoArgsConstructor
    public static class Body {
        @JacksonXmlProperty(localName = "numOfRows")
        private int numOfRows;

        @JacksonXmlProperty(localName = "pageNo")
        private int pageNo;

        @JacksonXmlProperty(localName = "totalCount")
        private int totalCount;

        private Items items;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @NoArgsConstructor
    public static class Items {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "item")
        private List<Item> item;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @NoArgsConstructor
    public static class Item {

        @JacksonXmlProperty(localName = "OBJT_ID") //일련번호
        private Long OBJT_ID;

        @JacksonXmlProperty(localName = "FCLTY_TY") // 유형명
        private String FCLTY_TY;

        @JacksonXmlProperty(localName = "MNG_INST") // 관리기관명
        private String MNG_INST;

        @JacksonXmlProperty(localName = "INS_PURPOS") // 설치 목적
        private String INS_PURPOS;

        @JacksonXmlProperty(localName = "INS_TYPE") // 설치장소 유형
        private String INS_TYPE;

        @JacksonXmlProperty(localName = "INS_DETAIL") // 설치 위치
        private String INS_DETAIL;

        @JacksonXmlProperty(localName = "RN_ADRES") //도로명 주소
        private String RN_ADRES;

        @JacksonXmlProperty(localName = "ADRES") // 지번 주소
        private String ADRES;

        @JacksonXmlProperty(localName = "LAT") // 위도
        private Double LAT;

        @JacksonXmlProperty(localName = "LON") //경도
        private Double LON;

        @JacksonXmlProperty(localName = "LNK_TYPE") // 연계방식
        private String LNK_TYPE;

        @JacksonXmlProperty(localName = "FLAG_POL_L") // 경찰 연계유무
        private String FLAG_POL_L;

        @JacksonXmlProperty(localName = "FLAG_SEC_L") // 경비업체 연계유무
        private String FLAG_SEC_L;

        @JacksonXmlProperty(localName = "FLAG_MNG_L") //관리사무소 연계유무
        private String FLAG_MNG_L;

        @JacksonXmlProperty(localName = "ADDITION") // 부가기능
        private String ADDITION;

        @JacksonXmlProperty(localName = "INS_YEAR") //설치연도
        private Long INS_YEAR;

        @JacksonXmlProperty(localName = "LAST_INSPD") //최종점검일자
        private String LAST_INSPD;

        @JacksonXmlProperty(localName = "LAST_INSPT") // 최종점검결과구분
        private String LAST_INSPT;

        @JacksonXmlProperty(localName = "MNG_TEL") // 관리기관전화번호
        private String MNG_TEL;

        @JacksonXmlProperty(localName = "FLAG_SERVI") // 연계유무
        private String FLAG_SERVI;

        @JacksonXmlProperty(localName = "CTPRVN_CD") // 시도코드
        private Long CTPRVN_CD;

        @JacksonXmlProperty(localName = "SGG_CD") // 시군구코드
        private Long SGG_CD;

        @JacksonXmlProperty(localName = "EMD_CD") // 읍면동코드
        private Long EMD_CD;

        @JacksonXmlProperty(localName = "X") // X좌표
        private Double X;

        @JacksonXmlProperty(localName = "Y") // Y좌표
        private Double Y;

        @JacksonXmlProperty(localName = "DATA_TY") // 데이터 기준값
        private Long DATA_TY;
    }
}

