package com.selfbell.criminal.controller;

import com.selfbell.criminal.dto.CriminalApiXmlDto;
import com.selfbell.criminal.service.CriminalApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
