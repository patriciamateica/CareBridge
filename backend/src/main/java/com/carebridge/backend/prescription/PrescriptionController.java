package com.carebridge.backend.prescription;

import com.carebridge.backend.prescription.model.PrescriptionDto;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;
    private final PrescriptionMapper mapper;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public PrescriptionController(PrescriptionService prescriptionService, PrescriptionMapper mapper,
                                  UserService userService, SimpMessagingTemplate messagingTemplate
    ) {
        this.prescriptionService = prescriptionService;
        this.mapper = mapper;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public Page<PrescriptionDto> getAll(Pageable pageable) {
        return prescriptionService.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public PrescriptionDto getById(@PathVariable UUID id) {
        return mapper.toDto(prescriptionService.getById(id));
    }

    @GetMapping("/patient/{patientId}")
    public Page<PrescriptionDto> getByPatientId(@PathVariable UUID patientId, Pageable pageable) {
        return prescriptionService.findByPatientId(patientId, pageable).map(mapper::toDto);
    }

    @PostMapping
    public PrescriptionDto create(@RequestBody PrescriptionDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (!patient.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (!nurse.hasRole("NURSE")) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        PrescriptionDto savedDto = mapper.toDto(prescriptionService.create(mapper.toEntity(dto, nurse, patient)));
        messagingTemplate.convertAndSend("/topic/prescriptions", savedDto);
        return savedDto;
    }

    @PutMapping("/{id}")
    public PrescriptionDto update(@PathVariable UUID id, @RequestBody PrescriptionDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (!patient.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (!nurse.hasRole("NURSE")) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        PrescriptionDto updatedDto = mapper.toDto(prescriptionService.update(id, mapper.toEntity(dto, nurse, patient)));
        messagingTemplate.convertAndSend("/topic/prescriptions", updatedDto);
        return updatedDto;
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        boolean deleted = prescriptionService.delete(id);
        if (deleted) {
            messagingTemplate.convertAndSend("/topic/prescriptions/deleted", java.util.Map.of("id", id.toString()));
        }
        return deleted;
    }
}
