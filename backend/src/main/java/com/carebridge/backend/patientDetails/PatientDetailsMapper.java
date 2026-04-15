package com.carebridge.backend.patientDetails;

import com.carebridge.backend.patientDetails.model.PatientDetails;
import com.carebridge.backend.patientDetails.model.PatientDetailsDto;
import org.springframework.stereotype.Component;

@Component
public class PatientDetailsMapper {

    public PatientDetailsDto toDto(PatientDetails details) {
        return new PatientDetailsDto(
            details.getId(),
            details.getUserId(),
            details.getPrimaryDiagnosis(),
            details.getDiagnostics(),
            details.getScans(),
            details.getEmergencyContact(),
            details.getAssignedNurseId()
        );
    }

    public PatientDetails toEntity(PatientDetailsDto dto) {
        return new PatientDetails(
            dto.id(),
            dto.userId(),
            dto.primaryDiagnosis(),
            dto.diagnostics(),
            dto.scans(),
            dto.emergencyContact(),
            dto.assignedNurseId()
        );
    }
}
