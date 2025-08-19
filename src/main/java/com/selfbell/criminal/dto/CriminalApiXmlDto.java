package com.selfbell.criminal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@JacksonXmlRootElement(localName = "response")
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@NoArgsConstructor
public class CriminalApiXmlDto {

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
        @JacksonXmlProperty(localName = "resultType")
        private String resultType;

        @JacksonXmlProperty(localName = "numOfRows")
        private Integer numOfRows;

        @JacksonXmlProperty(localName = "totalCount")
        private Integer totalCount;

        @JacksonXmlProperty(localName = "pageNo")
        private Integer pageNo;

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
        @JacksonXmlProperty(localName = "dataCrtYmd")
        private String dataCrtYmd;

        @JacksonXmlProperty(localName = "stdgCd")
        private String stdgCd;

        @JacksonXmlProperty(localName = "mtnYn")
        private String mtnYn;

        @JacksonXmlProperty(localName = "mno")
        private String mno;

        @JacksonXmlProperty(localName = "sno")
        private String sno;

        @JacksonXmlProperty(localName = "stdgCtpvSggCd")
        private String stdgCtpvSggCd;

        @JacksonXmlProperty(localName = "stdgEmdCd")
        private String stdgEmdCd;

        @JacksonXmlProperty(localName = "roadNmNo")
        private String roadNmNo;

        @JacksonXmlProperty(localName = "udgdYn")
        private String udgdYn;

        @JacksonXmlProperty(localName = "bmno")
        private String bmno;

        @JacksonXmlProperty(localName = "bsno")
        private String bsno;

        @JacksonXmlProperty(localName = "ctpvNm")
        private String ctpvNm;

        @JacksonXmlProperty(localName = "sggNm")
        private String sggNm;

        @JacksonXmlProperty(localName = "umdNm")
        private String umdNm;

        @JacksonXmlProperty(localName = "stliNm")
        private String stliNm;

        @JacksonXmlProperty(localName = "rprsLotnoYn")
        private String rprsLotnoYn;
    }
}
