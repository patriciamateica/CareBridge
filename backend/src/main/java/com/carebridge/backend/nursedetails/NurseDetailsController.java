package com.carebridge.backend.nursedetails;

import com.carebridge.backend.nursedetails.model.NurseDetailsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/nurse-details")
public class NurseDetailsController {
    private final NurseDetailsService service;
    private final NurseDetailsMapper mapper;

    public NurseDetailsController(NurseDetailsService service, NurseDetailsMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<NurseDetailsDto> getAll(Pageable pageable) {
        return service.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public NurseDetailsDto getById(@PathVariable UUID id) {
        return mapper.toDto(service.getById(id));
    }

    @PostMapping
    public NurseDetailsDto create(@RequestBody NurseDetailsDto dto) {
        return mapper.toDto(service.create(mapper.toEntity(dto)));
    }

    @PutMapping("/{id}")
    public NurseDetailsDto update(@PathVariable UUID id, @RequestBody NurseDetailsDto dto) {
        return mapper.toDto(service.update(id, mapper.toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}
