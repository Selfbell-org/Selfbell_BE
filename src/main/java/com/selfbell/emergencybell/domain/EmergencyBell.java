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

    // 외부 API의 OBJT_ID 를 PK로 사용 (자동생성 아님)
    @Id
    @Column(name = "id")
    private Long id;

    // 9개 필드 (DB 컬럼명은 프로젝트 DB 스키마에 맞게 조정)
    @Column(name = "lon")
    private Double lon; // lon

    @Column(name = "lat")
    private Double lat;  // lat

    @Column(name = "ins_DETAIL")
    private String ins_DETAIL; // ins_DETAIL

    @Column(name = "mng_TEL")
    private String mng_TEL; // mng_TEL

    @Column(name = "adres")
    private String adres; // adres

    @Column(name = "ins_TYPE")
    private String ins_TYPE; // ins_TYPE

    @Column(name = "x")
    private Double x; // x

    @Column(name = "y")
    private Double y; // y

    // objt id은 PK(id)와 동일하므로 별도 필드 불필요

    // 편의 업데이트 메서드 (DTO에서 값을 받아 필드만 갱신)
    public void updateFromItem(Long objtId,
                               Double lat,
                               Double lon,
                               String insDetail,
                               String mngTel,
                               String adres,
                               String insType,
                               Double x,
                               Double y) {
        // objtId는 PK; 새 엔티티 생성 시 이미 id에 세팅되어 있어야 함
        this.lat = lat;
        this.lon = lon;
        this.ins_DETAIL= insDetail;
        this.mng_TEL = mngTel;
        this.adres = adres;
        this.ins_TYPE = insType;
        this.x = x;
        this.y = y;
    }
}
