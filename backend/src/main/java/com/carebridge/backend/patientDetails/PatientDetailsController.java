package com.carebridge.backend.patientDetails;

import com.carebridge.backend.patientDetails.model.PatientDetailsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/patient-details")
public class PatientDetailsController {
    private final PatientDetailsService service;
    private final PatientDetailsMapper mapper;

    public PatientDetailsController(PatientDetailsService service, PatientDetailsMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<PatientDetailsDto> getAll(Pageable pageable) {
        return service.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public PatientDetailsDto getById(@PathVariable UUID id) {
        return mapper.toDto(service.getById(id));
    }

    @PostMapping
    public PatientDetailsDto create(@RequestBody PatientDetailsDto dto) {
        return mapper.toDto(service.create(mapper.toEntity(dto)));
    }

    @PutMapping("/{id}")
    public PatientDetailsDto update(@PathVariable UUID id, @RequestBody PatientDetailsDto dto) {
        return mapper.toDto(service.update(id, mapper.toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}
