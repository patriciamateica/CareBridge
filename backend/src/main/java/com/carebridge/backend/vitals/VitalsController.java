package com.carebridge.backend.vitals;

import com.carebridge.backend.vitals.model.VitalsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/vitals")
public class VitalsController {
    private final VitalsService vitalsService;
    private final VitalsMapper mapper;

    public VitalsController(VitalsService vitalsService, VitalsMapper mapper) {
        this.vitalsService = vitalsService;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<VitalsDto> getAllVitals(Pageable pageable) {
        return vitalsService.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public VitalsDto getById(@PathVariable UUID id) {
        return mapper.toDto(vitalsService.getById(id));
    }

    @PostMapping
    public VitalsDto create(@RequestBody VitalsDto vitalsDto) {
        return mapper.toDto(vitalsService.create(mapper.toEntity(vitalsDto)));
    }

    @PutMapping("/{id}")
    public VitalsDto update(@PathVariable UUID id, @RequestBody VitalsDto vitalsDto) {
        return mapper.toDto(vitalsService.update(id, mapper.toEntity(vitalsDto)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return vitalsService.delete(id);
    }
}
