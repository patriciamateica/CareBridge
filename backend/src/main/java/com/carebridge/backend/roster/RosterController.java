package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.RosterDto;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rosters")
public class RosterController {
    private final RosterService rosterService;
    private final RosterMapper mapper;
    private final UserService userService;

    public RosterController(RosterService rosterService, RosterMapper mapper, UserService userService) {
        this.rosterService = rosterService;
        this.mapper = mapper;
        this.userService = userService;
    }

    @GetMapping
    public Page<RosterDto> getAll(Pageable pageable) {
        return rosterService.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public RosterDto getById(@PathVariable UUID id) {
        return mapper.toDto(rosterService.getById(id));
    }

    @PostMapping
    public RosterDto create(@RequestBody RosterDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (patient.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (nurse.getRole() != Role.NURSE) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        return mapper.toDto(rosterService.create(mapper.toEntity(dto, nurse, patient)));
    }

    @PutMapping("/{id}")
    public RosterDto update(@PathVariable UUID id, @RequestBody RosterDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (patient.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (nurse.getRole() != Role.NURSE) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        return mapper.toDto(rosterService.update(id, mapper.toEntity(dto, nurse, patient)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return rosterService.delete(id);
    }
}
