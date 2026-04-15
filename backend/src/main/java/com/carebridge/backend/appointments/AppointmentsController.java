package com.carebridge.backend.appointments;

import com.carebridge.backend.appointments.model.AppointmentsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentsController {
    private final AppointmentsService appointmentsService;
    private final AppointmentsMapper mapper;

    public AppointmentsController(AppointmentsService appointmentsService, AppointmentsMapper mapper) {
        this.appointmentsService = appointmentsService;
        this.mapper = mapper;
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
        return mapper.toDto(appointmentsService.create(mapper.toEntity(dto)));
    }

    @PutMapping("/{id}")
    public AppointmentsDto update(@PathVariable UUID id, @RequestBody AppointmentsDto dto) {
        return mapper.toDto(appointmentsService.update(id, mapper.toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return appointmentsService.delete(id);
    }
}
