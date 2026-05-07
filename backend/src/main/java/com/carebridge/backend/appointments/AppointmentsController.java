package com.carebridge.backend.appointments;

import com.carebridge.backend.appointments.model.AppointmentsDto;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentsController {
    private final AppointmentsService appointmentsService;
    private final AppointmentsMapper mapper;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public AppointmentsController(AppointmentsService appointmentsService, AppointmentsMapper mapper, UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.appointmentsService = appointmentsService;
        this.mapper = mapper;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public Page<AppointmentsDto> getAll(Pageable pageable) {
        return appointmentsService.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public AppointmentsDto getById(@PathVariable UUID id) {
        return mapper.toDto(appointmentsService.getById(id));
    }

    @GetMapping("/patient/{patientId}")
    public Page<AppointmentsDto> getByPatientId(@PathVariable UUID patientId, Pageable pageable) {
        return appointmentsService.findByPatientId(patientId, pageable).map(mapper::toDto);
    }

    @GetMapping("/nurse/{nurseId}")
    public Page<AppointmentsDto> getByNurseId(@PathVariable UUID nurseId, Pageable pageable) {
        return appointmentsService.findByNurseId(nurseId, pageable).map(mapper::toDto);
    }

    @PostMapping
    public AppointmentsDto create(@RequestBody AppointmentsDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (!patient.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (!nurse.hasRole("NURSE")) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }

        AppointmentsDto savedDto = mapper.toDto(appointmentsService.create(mapper.toEntity(dto, nurse, patient)));
        messagingTemplate.convertAndSend("/topic/appointments", savedDto);
        return savedDto;
    }

    @PutMapping("/{id}")
    public AppointmentsDto update(@PathVariable UUID id, @RequestBody AppointmentsDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (!patient.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (!nurse.hasRole("NURSE")) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }

        AppointmentsDto updatedDto = mapper.toDto(appointmentsService.update(id, mapper.toEntity(dto, nurse, patient)));
        messagingTemplate.convertAndSend("/topic/appointments", updatedDto);
        return updatedDto;
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        boolean deleted = appointmentsService.delete(id);
        if (deleted) {
            messagingTemplate.convertAndSend("/topic/appointments/deleted", java.util.Map.of("id", id.toString()));
        }
        return deleted;
    }
}
