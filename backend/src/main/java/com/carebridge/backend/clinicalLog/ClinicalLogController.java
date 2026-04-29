package com.carebridge.backend.clinicalLog;

import com.carebridge.backend.clinicalLog.model.ClinicalLogDto;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/clinical-logs")
public class ClinicalLogController {
    private final ClinicalLogService service;
    private final ClinicalLogMapper mapper;
    private final UserService userService;

    public ClinicalLogController(ClinicalLogService service, ClinicalLogMapper mapper, UserService userService) {
        this.service = service;
        this.mapper = mapper;
        this.userService = userService;
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
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (patient.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (nurse.getRole() != Role.NURSE) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        return mapper.toDto(service.create(mapper.toEntity(dto, nurse, patient)));
    }

    @PutMapping("/{id}")
    public ClinicalLogDto update(@PathVariable UUID id, @RequestBody ClinicalLogDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (patient.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (nurse.getRole() != Role.NURSE) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        return mapper.toDto(service.update(id, mapper.toEntity(dto, nurse, patient)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}
