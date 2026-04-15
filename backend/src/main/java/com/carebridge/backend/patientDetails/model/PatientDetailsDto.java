package com.carebridge.backend.patientDetails.model;

import java.util.List;
import java.util.UUID;

public record PatientDetailsDto(
    UUID id,
    UUID userId,
    String primaryDiagnosis,
    List<String> diagnostics,
    List<String> scans,
    String emergencyContact,
    UUID assignedNurseId
) {
}
