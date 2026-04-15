package com.carebridge.backend.prescription;

import com.carebridge.backend.prescription.model.PrescriptionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;
    private final PrescriptionMapper mapper;

    public PrescriptionController(PrescriptionService prescriptionService, PrescriptionMapper mapper) {
        this.prescriptionService = prescriptionService;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<PrescriptionDto> getAll(Pageable pageable) {
        return prescriptionService.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public PrescriptionDto getById(@PathVariable UUID id) {
        return mapper.toDto(prescriptionService.getById(id));
    }

    @PostMapping
    public PrescriptionDto create(@RequestBody PrescriptionDto dto) {
        return mapper.toDto(prescriptionService.create(mapper.toEntity(dto)));
    }

    @PutMapping("/{id}")
    public PrescriptionDto update(@PathVariable UUID id, @RequestBody PrescriptionDto dto) {
        return mapper.toDto(prescriptionService.update(id, mapper.toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return prescriptionService.delete(id);
    }
}
