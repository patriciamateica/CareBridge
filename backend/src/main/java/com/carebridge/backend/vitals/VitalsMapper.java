package com.carebridge.backend.vitals;

import com.carebridge.backend.vitals.model.Vitals;
import com.carebridge.backend.vitals.model.VitalsDto;
import org.springframework.stereotype.Component;

@Component
public class VitalsMapper {
    public VitalsDto toDto(Vitals vitals) {
        return new VitalsDto(vitals.getId(), vitals.getTimestamp(), vitals.getHeartRate(), vitals.getBloodPressure(),
            vitals.getRespiratoryRate(), vitals.getSpO2(), vitals.getPatientId());
    }

    public Vitals toEntity(VitalsDto dto) {
        return new Vitals(dto.id(), dto.timestamp(), dto.heartRate(), dto.bloodPressure(), dto.respiratoryRate(),
            dto.spO2(), dto.patientId());
    }
}
