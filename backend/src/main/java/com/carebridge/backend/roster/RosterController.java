package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.RosterDto;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rosters")
public class RosterController {
    private final RosterService rosterService;
    private final RosterMapper mapper;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public RosterController(RosterService rosterService, RosterMapper mapper, UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.rosterService = rosterService;
        this.mapper = mapper;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public Page<RosterDto> getAll(Pageable pageable) {
        return rosterService.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public RosterDto getById(@PathVariable UUID id) {
        return mapper.toDto(rosterService.getById(id));
    }

    @GetMapping("/nurse/{nurseId}")
    public Page<RosterDto> getByNurseId(@PathVariable UUID nurseId, Pageable pageable) {
        return rosterService.findByNurseId(nurseId, pageable).map(mapper::toDto);
    }

    @PostMapping
    public RosterDto create(@RequestBody RosterDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (!patient.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (!nurse.hasRole("NURSE")) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        RosterDto savedDto = mapper.toDto(rosterService.create(mapper.toEntity(dto, nurse, patient)));
        messagingTemplate.convertAndSend("/topic/rosters", savedDto);
        return savedDto;
    }

    @PutMapping("/{id}")
    public RosterDto update(@PathVariable UUID id, @RequestBody RosterDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User nurse = userService.getUserById(dto.nurseId());

        if (!patient.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (!nurse.hasRole("NURSE")) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        RosterDto updatedDto = mapper.toDto(rosterService.update(id, mapper.toEntity(dto, nurse, patient)));
        messagingTemplate.convertAndSend("/topic/rosters", updatedDto);
        return updatedDto;
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        boolean deleted = rosterService.delete(id);
        if (deleted) {
            messagingTemplate.convertAndSend("/topic/rosters/deleted", java.util.Map.of("id", id.toString()));
        }
        return deleted;
    }
}
