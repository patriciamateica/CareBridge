package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.RosterDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rosters")
public class RosterController {
    private final RosterService rosterService;
    private final RosterMapper mapper;

    public RosterController(RosterService rosterService, RosterMapper mapper) {
        this.rosterService = rosterService;
        this.mapper = mapper;
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
        return mapper.toDto(rosterService.create(mapper.toEntity(dto)));
    }

    @PutMapping("/{id}")
    public RosterDto update(@PathVariable UUID id, @RequestBody RosterDto dto) {
        return mapper.toDto(rosterService.update(id, mapper.toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return rosterService.delete(id);
    }
}
