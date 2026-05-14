package com.carebridge.backend.task;

import com.carebridge.backend.task.model.TaskDto;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService service;
    private final TaskMapper mapper;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public TaskController(TaskService service, TaskMapper mapper, UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.service = service;
        this.mapper = mapper;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public Page<TaskDto> getAll(Pageable pageable) {
        return service.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public TaskDto getById(@PathVariable UUID id) {
        return mapper.toDto(service.getById(id));
    }

    @GetMapping("/patient/{patientId}")
    public Page<TaskDto> getByPatientId(@PathVariable UUID patientId, Pageable pageable) {
        return service.findByPatientId(patientId, pageable).map(mapper::toDto);
    }

    @GetMapping("/nurse/{nurseId}")
    public Page<TaskDto> getByNurseId(@PathVariable UUID nurseId, Pageable pageable) {
        return service.findByNurseId(nurseId, pageable).map(mapper::toDto);
    }

    @PostMapping
    public TaskDto create(@RequestBody TaskDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User claimer = dto.claimerId() != null ? userService.getUserById(dto.claimerId()) : null;

        if (!patient.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (claimer != null && !claimer.hasRole("NURSE") && !claimer.hasRole("FAMILY")) {
            throw new IllegalArgumentException("The provided claimer ID does not belong to a nurse or family.");
        }
        TaskDto savedDto = mapper.toDto(service.create(mapper.toEntity(dto, claimer, patient)));
        messagingTemplate.convertAndSend("/topic/tasks", savedDto);
        return savedDto;
    }

    @PutMapping("/{id}")
    public TaskDto update(@PathVariable UUID id, @RequestBody TaskDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User claimer = dto.claimerId() != null ? userService.getUserById(dto.claimerId()) : null;

        if (!patient.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (claimer != null && !claimer.hasRole("NURSE") && !claimer.hasRole("FAMILY")) {
            throw new IllegalArgumentException("The provided claimer ID does not belong to a nurse or family.");
        }
        TaskDto updatedDto = mapper.toDto(service.update(id, mapper.toEntity(dto, claimer, patient)));
        messagingTemplate.convertAndSend("/topic/tasks", updatedDto);
        return updatedDto;
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        boolean deleted = service.delete(id);
        if (deleted) {
            messagingTemplate.convertAndSend("/topic/tasks/deleted", id.toString());
        }
        return deleted;
    }
}
