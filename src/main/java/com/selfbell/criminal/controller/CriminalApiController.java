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

    @GetMapping("/xml")
    public CriminalApiXmlDto getCriminalsXml(
            @RequestParam String ctpvNm,
            @RequestParam String sggNm,
            @RequestParam String umdNm
    ) throws Exception {
        return service.getCriminalDataXml(ctpvNm, sggNm, umdNm);
    }

    @GetMapping("/json")
    public String getCriminalsJson(
            @RequestParam String ctpvNm,
            @RequestParam String sggNm,
            @RequestParam String umdNm
    ) throws Exception {
        return service.getCriminalDataJson(ctpvNm, sggNm, umdNm);
    }

    // 지오코딩 -> 위도, 경도만 반환
    @GetMapping("/coords")
    public List<CriminalCoordDto> getCriminalCoords(
            @RequestParam String ctpvNm,
            @RequestParam String sggNm,
            @RequestParam String umdNm
    ) throws Exception {
        return service.getCriminalCoords(ctpvNm, sggNm, umdNm);
    }
}
