package com.selfbell.criminal.controller;

import com.selfbell.criminal.dto.CriminalApiXmlDto;
import com.selfbell.criminal.dto.CriminalCoordDto;
import com.selfbell.criminal.service.CriminalApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/criminals")
@RequiredArgsConstructor
@Slf4j
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

    /**
     * 사용자의 현 위치(lat/lng)와 반경(m)을 받아 반경 내 결과(거리 포함) 반환
     * - radius 기본값: 500
     * - 최대 허용: 1000 (초과 시 오류 로그 출력 후 1000으로 조정)
     */
    @GetMapping("/coords/nearby")
    public List<CriminalCoordDto> getNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(name = "radius", defaultValue = "500") int radius
    ) throws Exception {

        int effectiveRadius = radius;
        if (effectiveRadius > 1000) {
            log.error("반경 초과 입력: {}m, 최대 반경 1000m로 자동 조정합니다.", effectiveRadius);
            effectiveRadius = 1000;
        }

        return service.getNearbyCoords(lat, lon, effectiveRadius);
    }
}
