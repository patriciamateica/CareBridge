package com.carebridge.backend.healthStatus;

import com.carebridge.backend.healthStatus.model.HealthStatusDto;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/health-status")
public class HealthStatusController {
    private final HealthStatusService healthStatusService;
    private final HealthStatusMapper mapper;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public HealthStatusController(HealthStatusService healthStatusService, HealthStatusMapper mapper,
                                  UserService userService, SimpMessagingTemplate messagingTemplate
    ) {
        this.healthStatusService = healthStatusService;
        this.mapper = mapper;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public Page<HealthStatusDto> getAll(Pageable pageable) {
        return healthStatusService.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public HealthStatusDto getById(@PathVariable UUID id) {
        return mapper.toDto(healthStatusService.getById(id));
    }

    @GetMapping("/patient/{patientId}")
    public Page<HealthStatusDto> getByPatientId(@PathVariable UUID patientId, Pageable pageable) {
        return healthStatusService.getByPatientId(patientId, pageable).map(mapper::toDto);
    }

    @PostMapping
    public HealthStatusDto create(@RequestBody HealthStatusDto dto) {
        User patient = userService.getUserById(dto.patientId());
        if (!patient.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        HealthStatusDto savedDto = mapper.toDto(healthStatusService.create(mapper.toEntity(dto, patient)));
        messagingTemplate.convertAndSend("/topic/health-status", savedDto);
        return savedDto;
    }

    @PutMapping("/{id}")
    public HealthStatusDto update(@PathVariable UUID id, @RequestBody HealthStatusDto dto) {
        User patient = userService.getUserById(dto.patientId());
        if (!patient.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        HealthStatusDto updatedDto = mapper.toDto(healthStatusService.update(id, mapper.toEntity(dto, patient)));
        messagingTemplate.convertAndSend("/topic/health-status", updatedDto);
        return updatedDto;
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        boolean deleted = healthStatusService.delete(id);
        if (deleted) {
            messagingTemplate.convertAndSend("/topic/health-status/deleted", java.util.Map.of("id", id.toString()));
        }
        return deleted;
    }
}
