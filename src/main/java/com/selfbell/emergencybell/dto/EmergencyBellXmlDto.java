package com.selfbell.emergencybell.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@JacksonXmlRootElement(localName = "response")
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

    // Item: API에서 필요한 9개 필드만 파싱
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @NoArgsConstructor
    public static class Item {

        @JacksonXmlProperty(localName = "OBJT_ID")
        private Long OBJT_ID;

        @JacksonXmlProperty(localName = "LAT")
        private Double LAT;

        @JacksonXmlProperty(localName = "LON")
        private Double LON;

        @JacksonXmlProperty(localName = "INS_DETAIL")
        private String INS_DETAIL;

        @JacksonXmlProperty(localName = "MNG_TEL")
        private String MNG_TEL;

        @JacksonXmlProperty(localName = "ADRES")
        private String ADRES;

        @JacksonXmlProperty(localName = "INS_TYPE")
        private String INS_TYPE;

        @JacksonXmlProperty(localName = "X")
        private Double X;

        @JacksonXmlProperty(localName = "Y")
        private Double Y;
    }
}
