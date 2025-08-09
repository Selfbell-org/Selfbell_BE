package com.selfbell.emergencybell.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private Double distance; // optional, 반경조회시 미터 단위로 포함
}
