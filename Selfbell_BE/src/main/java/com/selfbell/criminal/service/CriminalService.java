package com.selfbell.criminal.service;

import com.selfbell.criminal.domain.Criminal;
import com.selfbell.criminal.dto.CriminalDto;
import com.selfbell.criminal.repository.CriminalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CriminalService {

    private final CriminalRepository criminalRepository;

    public CriminalDto create(CriminalDto dto) {
        Criminal saved = criminalRepository.save(dto.toEntity());
        return CriminalDto.from(saved);
    }

    public List<CriminalDto> findAll() {
        return criminalRepository.findAll().stream()
                .map(CriminalDto::from)
                .toList();
    }

    public CriminalDto findById(Long id) {
        Criminal criminal = criminalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 범죄자를 찾을 수 없습니다. id=" + id));
        return CriminalDto.from(criminal);
    }

    public void delete(Long id) {
        criminalRepository.deleteById(id);
    }
}
