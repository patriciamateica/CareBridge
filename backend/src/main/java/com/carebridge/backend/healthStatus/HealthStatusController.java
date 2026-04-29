package com.carebridge.backend.healthStatus;

import com.carebridge.backend.healthStatus.model.HealthStatusDto;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/health-status")
public class HealthStatusController {
    private final HealthStatusService healthStatusService;
    private final HealthStatusMapper mapper;
    private final UserService userService;

    public HealthStatusController(HealthStatusService healthStatusService, HealthStatusMapper mapper,
                                  UserService userService
    ) {
        this.healthStatusService = healthStatusService;
        this.mapper = mapper;
        this.userService = userService;
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
        User patient = userService.getUserById(dto.patientId());
        if (patient.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        return mapper.toDto(healthStatusService.create(mapper.toEntity(dto, patient)));
    }

    @PutMapping("/{id}")
    public HealthStatusDto update(@PathVariable UUID id, @RequestBody HealthStatusDto dto) {
        User patient = userService.getUserById(dto.patientId());
        if (patient.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        return mapper.toDto(healthStatusService.update(id, mapper.toEntity(dto, patient)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return healthStatusService.delete(id);
    }
}
