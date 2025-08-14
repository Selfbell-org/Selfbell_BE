package com.selfbell.emergencybell.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "emergency_bell")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyBell {

    // 외부 API의 OBJT_ID를 PK로 사용
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "lon")
    private Double lon;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "ins_DETAIL")
    private String ins_DETAIL;

    @Column(name = "mng_TEL")
    private String mng_TEL;

    @Column(name = "adres")
    private String adres;

    @Column(name = "ins_TYPE")
    private String ins_TYPE;

    @Column(name = "x")
    private Double x;

    @Column(name = "y")
    private Double y;

    public void updateFromItem(Long objtId,
                               Double lat,
                               Double lon,
                               String insDetail,
                               String mngTel,
                               String adres,
                               String insType,
                               Double x,
                               Double y) {
        this.lat = lat;
        this.lon = lon;
        this.ins_DETAIL = insDetail;
        this.mng_TEL = mngTel;
        this.adres = adres;
        this.ins_TYPE = insType;
        this.x = x;
        this.y = y;
    }
}
