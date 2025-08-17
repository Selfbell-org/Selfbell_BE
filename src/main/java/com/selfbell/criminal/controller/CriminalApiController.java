package com.selfbell.criminal.controller;

import com.selfbell.criminal.dto.CriminalApiXmlDto;
import com.selfbell.criminal.dto.CriminalCoordDto;
import com.selfbell.criminal.service.CriminalApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/criminals")
@RequiredArgsConstructor
public class CriminalApiController {

    private final CriminalApiService service;

    /** 시·구만 입력 → XML (pageNo=1, numOfRows=10) */
    @GetMapping("/xml")
    public CriminalApiXmlDto getCriminalsXml(
            @RequestParam String ctpvNm,
            @RequestParam String sggNm
    ) throws Exception {
        return service.getCriminalDataXml(ctpvNm, sggNm);
    }

    /** 시·구만 입력 → JSON 원문 */
    @GetMapping("/json")
    public String getCriminalsJson(
            @RequestParam String ctpvNm,
            @RequestParam String sggNm
    ) throws Exception {
        return service.getCriminalDataJson(ctpvNm, sggNm);
    }

    /** 시·구만 입력 → 해당 ‘구’ 전체를 지오코딩해 좌표 반환 */
    @GetMapping("/coords")
    public List<CriminalCoordDto> getCriminalCoords(
            @RequestParam String ctpvNm,
            @RequestParam String sggNm
    ) throws Exception {
        return service.getCriminalCoords(ctpvNm, sggNm);
    }

    /** 사용자의 현 위치(lat/lng)와 반경(m)만 받아 반경 내 결과를 반환(거리 포함) */
    @GetMapping("/coords/nearby")
    public List<CriminalCoordDto> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam int radius // meters
    ) throws Exception {
        return service.getNearbyCoords(lat, lng, radius);
    }
}
