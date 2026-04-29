package com.carebridge.backend.vitals;

import com.carebridge.backend.user.model.User;
import com.carebridge.backend.vitals.model.Vitals;
import com.carebridge.backend.vitals.model.VitalsDto;
import org.springframework.stereotype.Component;

@Component
public class VitalsMapper {
    public VitalsDto toDto(Vitals vitals) {
        return new VitalsDto(vitals.getId(), vitals.getTimestamp(), vitals.getHeartRate(), vitals.getBloodPressure(),
            vitals.getRespiratoryRate(), vitals.getSpO2(), vitals.getPatient().getId());
    }

    public Vitals toEntity(VitalsDto dto, User patient) {
        return new Vitals(dto.id(), dto.timestamp(), dto.heartRate(), dto.bloodPressure(), dto.respiratoryRate(),
            dto.spO2(), patient);
    }
}
