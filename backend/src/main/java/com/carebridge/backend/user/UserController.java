package com.carebridge.backend.user;

import com.carebridge.backend.patientDetails.model.PatientRegistrationRequest;
import com.carebridge.backend.security.RegisterRequest;
import com.carebridge.backend.user.model.User;
import com.carebridge.backend.user.model.UserDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserMapper userMapper;

    public UserController(UserService userService, SimpMessagingTemplate messagingTemplate, UserMapper userMapper) {
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    @com.carebridge.backend.audit.LogAction("Fetch Current User Profile")
    public ResponseEntity<UserDto> getCurrentUser(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof com.carebridge.backend.user.CustomUserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userMapper.mapToDto(userDetails.getUser()));
    }

    @PostMapping("/register")
    @com.carebridge.backend.audit.LogAction("User Registration")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        User registeredUser = userService.register(request);
        UserDto dto = userMapper.mapToDto(registeredUser);
        messagingTemplate.convertAndSend("/topic/users", dto);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PostMapping("/register-patient")
    @com.carebridge.backend.audit.LogAction("Patient Registration")
    public ResponseEntity<UserDto> registerPatient(@Valid @RequestBody PatientRegistrationRequest request) {
        User registeredUser = userService.registerPatient(request);
        UserDto dto = userMapper.mapToDto(registeredUser);
        messagingTemplate.convertAndSend("/topic/users", dto);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userMapper.mapToDto(userService.getUserById(id)));
    }

    @GetMapping
    public ResponseEntity<Page<UserDto>> getUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersPaginated(pageable).map(userMapper::mapToDto));
    }

    @GetMapping("/by-role")
    public ResponseEntity<Page<UserDto>> getUsersByRole(@RequestParam String role, Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersByRolePaginated(role, pageable).map(userMapper::mapToDto));
    }

    @PutMapping("/{id}")
    @com.carebridge.backend.audit.LogAction("Update User")
    public ResponseEntity<UserDto> update(@PathVariable UUID id, @RequestBody User user) {
        User updatedUser = userService.update(id, user);
        UserDto dto = userMapper.mapToDto(updatedUser);
        messagingTemplate.convertAndSend("/topic/users", dto);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @com.carebridge.backend.audit.LogAction("Delete User")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        messagingTemplate.convertAndSend("/topic/users/deleted", java.util.Map.of("id", id.toString()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reactivate")
    @com.carebridge.backend.audit.LogAction("Reactivate User")
    public ResponseEntity<UserDto> reactivate(@PathVariable UUID id) {
        User reactUser = userService.updateStatus(id, com.carebridge.backend.user.UserStatus.ACTIVE);
        UserDto dto = userMapper.mapToDto(reactUser);
        messagingTemplate.convertAndSend("/topic/users", dto);
        return ResponseEntity.ok(dto);
    }
}
