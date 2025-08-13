package com.selfbell.emergencybell.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) //null 필드는 JSON에서 제외(= 상세정보에서 distance 빠짐)
public class EmergencyBellSummaryDto {

    @JsonProperty("lon")
    private Double lon;

    @JsonProperty("lat")
    private Double lat;

    @JsonProperty("ins_DETAIL")
    private String insDetail;

    @JsonProperty("objt_ID")
    private Long objtId;

    @JsonProperty("mng_TEL")
    private String mngTel;

    @JsonProperty("adres")
    private String adres;

    @JsonProperty("ins_TYPE")
    private String insType;

    @JsonProperty("x")
    private Double x;

    @JsonProperty("y")
    private Double y;

    @JsonProperty("distance")
    private Double distance; // 반경 조회시에만 세팅
}