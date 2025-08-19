package com.selfbell.emergencybell.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmergencyBellSummaryDto {

    @JsonProperty("lat")
    private double lat;

    @JsonProperty("lon")
    private double lon;

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

    @JsonProperty("distance")
    private Double distance; // 반경 조회시에만 세팅
}
