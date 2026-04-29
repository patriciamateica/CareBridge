package com.carebridge.backend.prescription;

import com.carebridge.backend.prescription.model.Prescription;
import com.carebridge.backend.prescription.model.PrescriptionDto;
import com.carebridge.backend.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class PrescriptionMapper {

    public PrescriptionDto toDto(Prescription prescription) {
        return new PrescriptionDto(
            prescription.getId(),
            prescription.getName(),
            prescription.getDose(),
            prescription.getTiming(),
            prescription.getPatient().getId(),
            prescription.getNurse().getId()
        );
    }

    public Prescription toEntity(PrescriptionDto dto, User nurse, User patient) {
        return new Prescription(
            dto.id(),
            dto.name(),
            dto.dose(),
            dto.timing(),
            patient,
            nurse
        );
    }
}
