package com.carebridge.backend.nursedetails;

import com.carebridge.backend.nursedetails.model.NurseDetailsDto;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/nurse-details")
public class NurseDetailsController {
    private final NurseDetailsService service;
    private final NurseDetailsMapper mapper;
    private final UserService userService;

    public NurseDetailsController(NurseDetailsService service, NurseDetailsMapper mapper, UserService userService) {
        this.service = service;
        this.mapper = mapper;
        this.userService = userService;
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
        User user = userService.getUserById(dto.userId());
        if (user.getRole() != Role.NURSE) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        return mapper.toDto(service.create(mapper.toEntity(dto, user)));
    }

    @PutMapping("/{id}")
    public NurseDetailsDto update(@PathVariable UUID id, @RequestBody NurseDetailsDto dto) {
        User user = userService.getUserById(dto.userId());
        if (user.getRole() != Role.NURSE) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        return mapper.toDto(service.update(id, mapper.toEntity(dto, user)));
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}
