package com.selfbell.criminal.controller;

import com.selfbell.criminal.dto.CriminalDto;
import com.selfbell.criminal.service.CriminalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/criminals")
@RequiredArgsConstructor
public class CriminalController {

    private final CriminalService criminalService;

    @PostMapping
    public ResponseEntity<CriminalDto> create(@RequestBody CriminalDto dto) {
        return ResponseEntity.ok(criminalService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<CriminalDto>> getAll() {
        return ResponseEntity.ok(criminalService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CriminalDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(criminalService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        criminalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
