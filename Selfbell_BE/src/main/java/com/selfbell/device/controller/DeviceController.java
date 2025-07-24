package com.selfbell.device.controller;

import com.selfbell.device.dto.DeviceRequestDto;
import com.selfbell.device.dto.DeviceResponseDto;
import com.selfbell.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    public DeviceResponseDto registerDevice(@RequestBody DeviceRequestDto dto) {
        return deviceService.registerDevice(dto);
    }
}
