package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotesDto;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/care-notes")
public class CareNotesController {
    private final CareNotesService service;
    private final CareNotesMapper mapper;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public CareNotesController(CareNotesService service, CareNotesMapper mapper, UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.service = service;
        this.mapper = mapper;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public Page<CareNotesDto> getAll(Pageable pageable) {
        return service.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public CareNotesDto getById(@PathVariable UUID id) {
        return mapper.toDto(service.getById(id));
    }

    @GetMapping("/patient/{patientId}")
    public Page<CareNotesDto> getByPatientId(@PathVariable UUID patientId, Pageable pageable) {
        return service.findByPatientId(patientId, pageable).map(mapper::toDto);
    }

    @PostMapping
    public CareNotesDto create(@RequestBody CareNotesDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (!patient.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (!nurse.hasRole("NURSE")) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }

        CareNotesDto savedDto = mapper.toDto(service.create(mapper.toEntity(dto, nurse, patient)));
        messagingTemplate.convertAndSend("/topic/care-notes", savedDto);
        return savedDto;
    }

    @PutMapping("/{id}")
    public CareNotesDto update(@PathVariable UUID id, @RequestBody CareNotesDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (!patient.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (!nurse.hasRole("NURSE")) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }

        CareNotesDto updatedDto = mapper.toDto(service.update(id, mapper.toEntity(dto, nurse, patient)));
        messagingTemplate.convertAndSend("/topic/care-notes", updatedDto);
        return updatedDto;
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        boolean deleted = service.delete(id);
        if (deleted) {
            messagingTemplate.convertAndSend("/topic/care-notes/deleted", java.util.Map.of("id", id.toString()));
        }
        return deleted;
    }
}
