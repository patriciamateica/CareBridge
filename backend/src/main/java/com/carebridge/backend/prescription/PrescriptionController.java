package com.carebridge.backend.prescription;

import com.carebridge.backend.prescription.model.PrescriptionDto;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;
    private final PrescriptionMapper mapper;
    private final UserService userService;

    public PrescriptionController(PrescriptionService prescriptionService, PrescriptionMapper mapper,
                                  UserService userService
    ) {
        this.prescriptionService = prescriptionService;
        this.mapper = mapper;
        this.userService = userService;
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
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (patient.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (nurse.getRole() != Role.NURSE) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        return mapper.toDto(prescriptionService.create(mapper.toEntity(dto, nurse, patient)));
    }

    @PutMapping("/{id}")
    public PrescriptionDto update(@PathVariable UUID id, @RequestBody PrescriptionDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (patient.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (nurse.getRole() != Role.NURSE) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        return mapper.toDto(prescriptionService.update(id, mapper.toEntity(dto, nurse, patient)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return prescriptionService.delete(id);
    }
}
