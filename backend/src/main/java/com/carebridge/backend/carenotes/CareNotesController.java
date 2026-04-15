package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotesDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/care-notes")
public class CareNotesController {
    private final CareNotesService service;
    private final CareNotesMapper mapper;

    public CareNotesController(CareNotesService service, CareNotesMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<CareNotesDto> getAll(Pageable pageable) {
        return service.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public CareNotesDto getById(@PathVariable UUID id) {
        return mapper.toDto(service.getById(id));
    }

    @PostMapping
    public CareNotesDto create(@RequestBody CareNotesDto dto) {
        return mapper.toDto(service.create(mapper.toEntity(dto)));
    }

    @PutMapping("/{id}")
    public CareNotesDto update(@PathVariable UUID id, @RequestBody CareNotesDto dto) {
        return mapper.toDto(service.update(id, mapper.toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}
