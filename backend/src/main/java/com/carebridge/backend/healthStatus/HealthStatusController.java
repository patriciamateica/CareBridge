package com.carebridge.backend.healthStatus;

import com.carebridge.backend.healthStatus.model.HealthStatusDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/health-status")
public class HealthStatusController {
    private final HealthStatusService healthStatusService;
    private final HealthStatusMapper mapper;

    public HealthStatusController(HealthStatusService healthStatusService, HealthStatusMapper mapper) {
        this.healthStatusService = healthStatusService;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<HealthStatusDto> getAll(Pageable pageable) {
        return healthStatusService.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public HealthStatusDto getById(@PathVariable UUID id) {
        return mapper.toDto(healthStatusService.getById(id));
    }

    @PostMapping
    public HealthStatusDto create(@RequestBody HealthStatusDto dto) {
        return mapper.toDto(healthStatusService.create(mapper.toEntity(dto)));
    }

    @PutMapping("/{id}")
    public HealthStatusDto update(@PathVariable UUID id, @RequestBody HealthStatusDto dto) {
        return mapper.toDto(healthStatusService.update(id, mapper.toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return healthStatusService.delete(id);
    }
}
