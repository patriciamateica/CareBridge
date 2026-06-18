package com.carebridge.backend.clinicalLog;

import com.carebridge.backend.clinicalLog.model.ClinicalLogDto;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clinical-logs")
public class ClinicalLogController {
    private final ClinicalLogService service;
    private final ClinicalLogMapper mapper;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public ClinicalLogController(ClinicalLogService service, ClinicalLogMapper mapper, UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.service = service;
        this.mapper = mapper;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public Page<ClinicalLogDto> getAll(Pageable pageable) {
        return service.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public ClinicalLogDto getById(@PathVariable UUID id) {
        return mapper.toDto(service.getById(id));
    }

    @GetMapping("/patient/{patientId}")
    public List<ClinicalLogDto> getByPatientId(@PathVariable UUID patientId) {
        return service.getByPatientId(patientId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @PostMapping
    public ClinicalLogDto create(@RequestBody ClinicalLogDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (!patient.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (!nurse.hasRole("NURSE")) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        ClinicalLogDto savedDto = mapper.toDto(service.create(mapper.toEntity(dto, nurse, patient)));
        messagingTemplate.convertAndSend("/topic/clinical-logs", savedDto);
        return savedDto;
    }

    @PutMapping("/{id}")
    public ClinicalLogDto update(@PathVariable UUID id, @RequestBody ClinicalLogDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (!patient.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (!nurse.hasRole("NURSE")) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        ClinicalLogDto updatedDto = mapper.toDto(service.update(id, mapper.toEntity(dto, nurse, patient)));
        messagingTemplate.convertAndSend("/topic/clinical-logs", updatedDto);
        return updatedDto;
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        boolean deleted = service.delete(id);
        if (deleted) {
            messagingTemplate.convertAndSend("/topic/clinical-logs/deleted", id.toString());
        }
        return deleted;
    }
}
