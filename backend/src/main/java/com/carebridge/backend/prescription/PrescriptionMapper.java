package com.carebridge.backend.prescription;

import com.carebridge.backend.prescription.model.Prescription;
import com.carebridge.backend.prescription.model.PrescriptionDto;
import org.springframework.stereotype.Component;

@Component
public class PrescriptionMapper {

    public PrescriptionDto toDto(Prescription prescription) {
        return new PrescriptionDto(
            prescription.getId(),
            prescription.getName(),
            prescription.getDose(),
            prescription.getTiming(),
            prescription.getPatientId(),
            prescription.getNurseId()
        );
    }

    public Prescription toEntity(PrescriptionDto dto) {
        return new Prescription(
            dto.id(),
            dto.name(),
            dto.dose(),
            dto.timing(),
            dto.patientId(),
            dto.nurseId()
        );
    }
}
