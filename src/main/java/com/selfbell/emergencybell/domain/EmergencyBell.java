package com.selfbell.emergencybell.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "emergency_bell")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyBell {

    @Id
    @Column(name = "id")
    private Long id; // OBJT_ID

    @Column(name = "facility_type")
    private String facilityType; // FCLTY_TY

    @Column(name = "managing_institution")
    private String managingInstitution; // MNG_INST

    @Column(name = "install_purpose")
    private String installPurpose; // INS_PURPOS

    @Column(name = "install_type")
    private String installType; // INS_TYPE

    @Column(name = "install_detail")
    private String installDetail; // INS_DETAIL

    @Column(name = "road_address")
    private String roadAddress; // RN_ADRES

    @Column(name = "lot_number_address")
    private String lotNumberAddress; // ADRES

    @Column(name = "latitude")
    private Double latitude; // LAT

    @Column(name = "longitude")
    private Double longitude; // LON

    @Column(name = "link_type")
    private String linkType; // LNK_TYPE

    @Column(name = "police_linked")
    private String policeLinked; // FLAG_POL_L

    @Column(name = "security_linked")
    private String securityLinked; // FLAG_SEC_L

    @Column(name = "management_linked")
    private String managementLinked; // FLAG_MNG_L

    @Column(name = "addition")
    private String addition; // ADDITION

    @Column(name = "install_year")
    private Integer installYear; // INS_YEAR

    @Column(name = "last_inspection_date")
    private LocalDate lastInspectionDate; // LAST_INSPD

    @Column(name = "last_inspection_result")
    private String lastInspectionResult; // LAST_INSPT

    @Column(name = "management_phone")
    private String managementPhone; // MNG_TEL

    @Column(name = "service_linked")
    private String serviceLinked; // FLAG_SERVI

    @Column(name = "province_code")
    private Integer provinceCode; // CTPRVN_CD

    @Column(name = "district_code")
    private Integer districtCode; // SGG_CD

    @Column(name = "township_code")
    private Integer townshipCode; // EMD_CD

    @Column(name = "coord_x")
    private Double coordX; // X

    @Column(name = "coord_y")
    private Double coordY; // Y

    @Column(name = "data_type")
    private Integer dataType; // DATA_TY

    public void setLastInspectionDateFromString(String yyyymmdd) {
        if (yyyymmdd != null && yyyymmdd.length() == 8) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            this.lastInspectionDate = LocalDate.parse(yyyymmdd, formatter);
        }
    }
}

