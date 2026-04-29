package com.carebridge.backend.patientDetails;

import com.carebridge.backend.patientDetails.model.PatientDetails;
import com.carebridge.backend.patientDetails.model.PatientDetailsDto;
import com.carebridge.backend.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class PatientDetailsMapper {

    public PatientDetailsDto toDto(PatientDetails details) {
        return new PatientDetailsDto(
            details.getId(),
            details.getUser().getId(),
            details.getPrimaryDiagnosis(),
            details.getDiagnostics(),
            details.getScans(),
            details.getEmergencyContact(),
            details.getAssignedNurseId()
        );
    }

    public PatientDetails toEntity(PatientDetailsDto dto, User user) {
        return new PatientDetails(
            user,
            dto.primaryDiagnosis(),
            dto.diagnostics(),
            dto.scans(),
            dto.emergencyContact(),
            dto.assignedNurseId()
        );
    }
}
