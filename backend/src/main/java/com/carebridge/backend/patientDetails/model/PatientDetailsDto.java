package com.carebridge.backend.patientDetails.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record PatientDetailsDto(
    UUID id,

    @NotNull(message = "User ID linkage is mandatory")
    UUID userId,

    @NotBlank(message = "Primary diagnosis is required")
    String primaryDiagnosis,

    List<String> diagnostics,

    List<String> scans,

    @NotBlank(message = "Emergency contact information is mandatory")
    String emergencyContact,

    UUID assignedNurseId
) {}
