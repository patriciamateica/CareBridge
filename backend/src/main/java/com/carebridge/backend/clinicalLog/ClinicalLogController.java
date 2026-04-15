package com.carebridge.backend.clinicalLog;

import com.carebridge.backend.clinicalLog.model.ClinicalLogDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/clinical-logs")
public class ClinicalLogController {
    private final ClinicalLogService service;
    private final ClinicalLogMapper mapper;

    public ClinicalLogController(ClinicalLogService service, ClinicalLogMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<ClinicalLogDto> getAll(Pageable pageable) {
        return service.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public ClinicalLogDto getById(@PathVariable UUID id) {
        return mapper.toDto(service.getById(id));
    }

    @PostMapping
    public ClinicalLogDto create(@RequestBody ClinicalLogDto dto) {
        return mapper.toDto(service.create(mapper.toEntity(dto)));
    }

    @PutMapping("/{id}")
    public ClinicalLogDto update(@PathVariable UUID id, @RequestBody ClinicalLogDto dto) {
        return mapper.toDto(service.update(id, mapper.toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}
