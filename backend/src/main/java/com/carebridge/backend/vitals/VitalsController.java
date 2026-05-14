package com.carebridge.backend.vitals;

import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import com.carebridge.backend.vitals.model.VitalsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/vitals")
public class VitalsController {
    private final VitalsService vitalsService;
    private final VitalsMapper mapper;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public VitalsController(VitalsService vitalsService, VitalsMapper mapper, UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.vitalsService = vitalsService;
        this.mapper = mapper;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public Page<VitalsDto> getAllVitals(Pageable pageable) {
        return vitalsService.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public VitalsDto getById(@PathVariable UUID id) {
        return mapper.toDto(vitalsService.getById(id));
    }

    @GetMapping("/patient/{patientId}")
    public Page<VitalsDto> getByPatientId(@PathVariable UUID patientId, Pageable pageable) {
        return vitalsService.getByPatientId(patientId, pageable).map(mapper::toDto);
    }

    @PostMapping
    public VitalsDto create(@RequestBody VitalsDto vitalsDto) {
        User patient = userService.getUserById(vitalsDto.patientId());
        if (!patient.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        VitalsDto savedDto = mapper.toDto(vitalsService.create(mapper.toEntity(vitalsDto, patient)));
        messagingTemplate.convertAndSend("/topic/vitals", savedDto);
        return savedDto;
    }

    @PutMapping("/{id}")
    public VitalsDto update(@PathVariable UUID id, @RequestBody VitalsDto vitalsDto) {
        User patient = userService.getUserById(vitalsDto.patientId());
        if (!patient.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        VitalsDto updatedDto = mapper.toDto(vitalsService.update(id, mapper.toEntity(vitalsDto, patient)));
        messagingTemplate.convertAndSend("/topic/vitals", updatedDto);
        return updatedDto;
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        boolean deleted = vitalsService.delete(id);
        if (deleted) {
            messagingTemplate.convertAndSend("/topic/vitals/deleted", id.toString());
        }
        return deleted;
    }
}
