package com.carebridge.backend.nursedetails;

import com.carebridge.backend.nursedetails.model.NurseDetailsDto;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/nurse-details")
public class NurseDetailsController {
    private final NurseDetailsService service;
    private final NurseDetailsMapper mapper;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public NurseDetailsController(NurseDetailsService service, NurseDetailsMapper mapper, UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.service = service;
        this.mapper = mapper;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public Page<NurseDetailsDto> getAll(Pageable pageable) {
        return service.findAll(pageable).map(mapper::toDto);
    }

    @GetMapping("/{id}")
    public NurseDetailsDto getById(@PathVariable UUID id) {
        return mapper.toDto(service.getById(id));
    }

    @GetMapping("/by-user/{userId}")
    public NurseDetailsDto getByUserId(@PathVariable UUID userId) {
        return mapper.toDto(service.getByUserId(userId));
    }

    @PostMapping
    public NurseDetailsDto create(@RequestBody NurseDetailsDto dto) {
        User user = userService.getUserById(dto.userId());
        if (!user.hasRole("NURSE")) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        NurseDetailsDto savedDto = mapper.toDto(service.create(mapper.toEntity(dto, user)));
        messagingTemplate.convertAndSend("/topic/nurse-details", savedDto);
        return savedDto;
    }

    @PutMapping("/{id}")
    public NurseDetailsDto update(@PathVariable UUID id, @RequestBody NurseDetailsDto dto) {
        User user = userService.getUserById(dto.userId());
        if (!user.hasRole("NURSE")) {
            throw new IllegalArgumentException("The provided nurse ID does not belong to a nurse.");
        }
        NurseDetailsDto updatedDto = mapper.toDto(service.update(id, mapper.toEntity(dto, user)));
        messagingTemplate.convertAndSend("/topic/nurse-details", updatedDto);
        return updatedDto;
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable UUID id) {
        boolean deleted = service.delete(id);
        if (deleted) {
            messagingTemplate.convertAndSend("/topic/nurse-details/deleted", id.toString());
        }
        return deleted;
    }
}
