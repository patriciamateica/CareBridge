package com.carebridge.backend.patientDetails.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

public record PatientCreateDto(
    @NotBlank(message = "Account Email is required")
    @Email(message = "Invalid email format")
    String userEmail,

    @NotBlank(message = "Primary diagnosis is required")
    String primaryDiagnosis,

    List<String> diagnostics,

    List<String> scans,

    @NotBlank(message = "Emergency contact information is mandatory")
    String emergencyContact,

    UUID assignedNurseId,

    String status
) {
    public PatientCreateDto {
        if (diagnostics == null) diagnostics = List.of();
        if (scans == null) scans = List.of();
    }
}
