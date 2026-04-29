package com.carebridge.backend.appointments;

import com.carebridge.backend.appointments.model.AppointmentsDto;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentsController {
    private final AppointmentsService appointmentsService;
    private final AppointmentsMapper mapper;
    private final UserService userService;

    public AppointmentsController(AppointmentsService appointmentsService, AppointmentsMapper mapper, UserService userService) {
        this.appointmentsService = appointmentsService;
        this.mapper = mapper;
        this.userService = userService;
    }

    @GetMapping
    public Page<AppointmentsDto> getAll(Pageable pageable) {
        return appointmentsService.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public AppointmentsDto getById(@PathVariable UUID id) {
        return mapper.toDto(appointmentsService.getById(id));
    }

    @PostMapping
    public AppointmentsDto create(@RequestBody AppointmentsDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (patient.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (nurse.getRole() != Role.NURSE) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }

        return mapper.toDto(appointmentsService.create(mapper.toEntity(dto, nurse, patient)));
    }

    @PutMapping("/{id}")
    public AppointmentsDto update(@PathVariable UUID id, @RequestBody AppointmentsDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (patient.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (nurse.getRole() != Role.NURSE) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }

        return mapper.toDto(appointmentsService.update(id, mapper.toEntity(dto, patient, nurse)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return appointmentsService.delete(id);
    }
}
