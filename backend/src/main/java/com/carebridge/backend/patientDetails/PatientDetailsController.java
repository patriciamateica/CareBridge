package com.carebridge.backend.patientDetails;

import com.carebridge.backend.patientDetails.model.PatientDetailsDto;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/patient-details")
public class PatientDetailsController {
    private final PatientDetailsService service;
    private final PatientDetailsMapper mapper;
    private final UserService userService;

    public PatientDetailsController(PatientDetailsService service, PatientDetailsMapper mapper, UserService userService) {
        this.service = service;
        this.mapper = mapper;
        this.userService = userService;
    }

    @GetMapping
    public Page<PatientDetailsDto> getAll(Pageable pageable) {
        return service.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public PatientDetailsDto getById(@PathVariable UUID id) {
        return mapper.toDto(service.getById(id));
    }

    @PostMapping
    public PatientDetailsDto create(@RequestBody PatientDetailsDto dto) {
        User user = userService.getUserById(dto.userId());
        if (user.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        return mapper.toDto(service.create(mapper.toEntity(dto, user)));
    }

    @PutMapping("/{id}")
    public PatientDetailsDto update(@PathVariable UUID id, @RequestBody PatientDetailsDto dto) {
        User user = userService.getUserById(dto.userId());
        if (user.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        return mapper.toDto(service.update(id, mapper.toEntity(dto, user)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}
