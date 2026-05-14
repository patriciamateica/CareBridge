package com.carebridge.backend.patientDetails;

import com.carebridge.backend.patientDetails.model.PatientCreateDto;
import com.carebridge.backend.patientDetails.model.PatientDetails;
import com.carebridge.backend.patientDetails.model.PatientDetailsDto;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/patient-details")
public class PatientDetailsController {
    private final PatientDetailsService service;
    private final PatientDetailsMapper mapper;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public PatientDetailsController(PatientDetailsService service, PatientDetailsMapper mapper, UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.service = service;
        this.mapper = mapper;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public Page<PatientDetailsDto> getAll(Pageable pageable) {
        return service.findAll(pageable).map(this::convertToDto);
    }

    @GetMapping({"/{id}"})
    public PatientDetailsDto getById(@PathVariable UUID id) {
        return convertToDto(service.getById(id));
    }

    /**
     * Returns patient details for all patients assigned to the given nurse.
     * Used by the frontend to enforce nurse-scoped roster views without fetching all records.
     */
    @GetMapping("/nurse/{nurseId}")
    public Page<PatientDetailsDto> getByNurseId(@PathVariable UUID nurseId, Pageable pageable) {
        return service.findByNurseId(nurseId, pageable).map(this::convertToDto);
    }

    /**
     * Returns the single patient details record linked to the given user (patient) ID.
     * Used by the patient-detail view to load details without scanning all records.
     */
    @GetMapping("/user/{userId}")
    public PatientDetailsDto getByUserId(@PathVariable UUID userId) {
        return convertToDto(service.getByUserId(userId));
    }

    @PostMapping
    @com.carebridge.backend.audit.LogAction("Create Patient Details")
    public PatientDetailsDto create(@RequestBody PatientCreateDto dto) {
        PatientDetailsDto created = convertToDto(service.createPatient(dto));
        messagingTemplate.convertAndSend("/topic/patient-details", created);
        return created;
    }

    @PutMapping("/{id}")
    @com.carebridge.backend.audit.LogAction("Update Patient Details")
    public PatientDetailsDto update(@PathVariable UUID id, @RequestBody PatientDetailsDto dto) {
        User user = userService.getUserById(dto.userId());
        if (!user.hasRole("PATIENT")) {
            throw new IllegalArgumentException("The provided user ID does not belong to a patient.");
        }
        PatientDetails details = mapper.toEntity(dto, user);
        PatientDetailsDto updatedDto = convertToDto(service.update(id, details));
        messagingTemplate.convertAndSend("/topic/patient-details", updatedDto);
        return updatedDto;
    }

    private PatientDetailsDto convertToDto(PatientDetails details) {
        String nurseName = "Not Assigned";
        if (details.getAssignedNurseId() != null) {
            try {
                User nurse = userService.getUserById(details.getAssignedNurseId());
                nurseName = nurse.getFirstName() + " " + nurse.getLastName();
            } catch (Exception e) {
                nurseName = "Unknown Nurse";
            }
        }
        return mapper.toDto(details, nurseName);
    }

    @DeleteMapping("/{id}")
    @com.carebridge.backend.audit.LogAction("Delete Patient Details")
    public boolean delete(@PathVariable UUID id) {
        boolean deleted = service.delete(id);
        if (deleted) {
            messagingTemplate.convertAndSend("/topic/patient-details/deleted", java.util.Map.of("id", id.toString()));
        }
        return deleted;
    }
}
