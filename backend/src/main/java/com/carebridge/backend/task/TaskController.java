package com.carebridge.backend.task;

import com.carebridge.backend.task.model.TaskDto;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService service;
    private final TaskMapper mapper;
    private final UserService userService;

    public TaskController(TaskService service, TaskMapper mapper, UserService userService) {
        this.service = service;
        this.mapper = mapper;
        this.userService = userService;
    }

    @GetMapping
    public Page<TaskDto> getAll(Pageable pageable) {
        return service.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public TaskDto getById(@PathVariable UUID id) {
        return mapper.toDto(service.getById(id));
    }

    @PostMapping
    public TaskDto create(@RequestBody TaskDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User claimer = dto.claimerId() != null ? userService.getUserById(dto.claimerId()) : null;

        if (patient.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (claimer != null && claimer.getRole() != Role.NURSE && claimer.getRole() != Role.FAMILY) {
            throw new IllegalArgumentException("The provided claimer ID does not belong to a nurse or family.");
        }
        return mapper.toDto(service.create(mapper.toEntity(dto, claimer, patient)));
    }

    @PutMapping("/{id}")
    public TaskDto update(@PathVariable UUID id, @RequestBody TaskDto dto) {
        User patient = userService.getUserById(dto.patientId());
        User claimer = dto.claimerId() != null ? userService.getUserById(dto.claimerId()) : null;

        if (patient.getRole() != Role.PATIENT) {
            throw new IllegalArgumentException("The provided patient ID does not belong to a patient.");
        }
        if (claimer != null && claimer.getRole() != Role.NURSE && claimer.getRole() != Role.FAMILY) {
            throw new IllegalArgumentException("The provided claimer ID does not belong to a nurse or family.");
        }
        return mapper.toDto(service.update(id, mapper.toEntity(dto, claimer, patient)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}
