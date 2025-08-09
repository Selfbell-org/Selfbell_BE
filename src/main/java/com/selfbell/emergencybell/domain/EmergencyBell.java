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
    private Long id;

    @Column(name = "facility_type")
    private String facilityType;

    @Column(name = "managing_institution")
    private String managingInstitution;

    @Column(name = "install_purpose")
    private String installPurpose;

    @Column(name = "install_type")
    private String installType;

    @Column(name = "install_detail")
    private String installDetail;

    @Column(name = "road_address")
    private String roadAddress;

    @Column(name = "lot_number_address")
    private String lotNumberAddress;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "link_type")
    private String linkType;

    @Column(name = "police_linked")
    private String policeLinked;

    @Column(name = "security_linked")
    private String securityLinked;

    @Column(name = "management_linked")
    private String managementLinked;

    @Column(name = "addition")
    private String addition;

    @Column(name = "install_year")
    private Integer installYear;

    @Column(name = "last_inspection_date")
    private LocalDate lastInspectionDate;

    @Column(name = "last_inspection_result")
    private String lastInspectionResult;

    @Column(name = "management_phone")
    private String managementPhone;

    @Column(name = "service_linked")
    private String serviceLinked;

    @Column(name = "province_code")
    private Integer provinceCode;

    @Column(name = "district_code")
    private Integer districtCode;

    @Column(name = "township_code")
    private Integer townshipCode;

    @Column(name = "coord_x")
    private Double coordX;

    @Column(name = "coord_y")
    private Double coordY;

    @Column(name = "data_type")
    private Integer dataType;

    // 각 필드별 update 메서드
    public void updateFacilityType(String facilityType) { this.facilityType = facilityType; }
    public void updateManagingInstitution(String managingInstitution) { this.managingInstitution = managingInstitution; }
    public void updateInstallPurpose(String installPurpose) { this.installPurpose = installPurpose; }
    public void updateInstallType(String installType) { this.installType = installType; }
    public void updateInstallDetail(String installDetail) { this.installDetail = installDetail; }
    public void updateRoadAddress(String roadAddress) { this.roadAddress = roadAddress; }
    public void updateLotNumberAddress(String lotNumberAddress) { this.lotNumberAddress = lotNumberAddress; }
    public void updateLatitude(Double latitude) { this.latitude = latitude; }
    public void updateLongitude(Double longitude) { this.longitude = longitude; }
    public void updateLinkType(String linkType) { this.linkType = linkType; }
    public void updatePoliceLinked(String policeLinked) { this.policeLinked = policeLinked; }
    public void updateSecurityLinked(String securityLinked) { this.securityLinked = securityLinked; }
    public void updateManagementLinked(String managementLinked) { this.managementLinked = managementLinked; }
    public void updateAddition(String addition) { this.addition = addition; }
    public void updateInstallYear(Integer installYear) { this.installYear = installYear; }
    public void updateLastInspectionResult(String lastInspectionResult) { this.lastInspectionResult = lastInspectionResult; }
    public void updateManagementPhone(String managementPhone) { this.managementPhone = managementPhone; }
    public void updateServiceLinked(String serviceLinked) { this.serviceLinked = serviceLinked; }
    public void updateProvinceCode(Integer provinceCode) { this.provinceCode = provinceCode; }
    public void updateDistrictCode(Integer districtCode) { this.districtCode = districtCode; }
    public void updateTownshipCode(Integer townshipCode) { this.townshipCode = townshipCode; }
    public void updateCoordX(Double coordX) { this.coordX = coordX; }
    public void updateCoordY(Double coordY) { this.coordY = coordY; }
    public void updateDataType(Integer dataType) { this.dataType = dataType; }

    public void updateLastInspectionDateFromString(String yyyymmdd) {
        if (yyyymmdd != null && yyyymmdd.length() == 8) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            this.lastInspectionDate = LocalDate.parse(yyyymmdd, formatter);
        }
    }
}
