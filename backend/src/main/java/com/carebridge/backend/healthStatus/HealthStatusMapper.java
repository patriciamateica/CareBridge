package com.carebridge.backend.healthStatus;

import com.carebridge.backend.healthStatus.model.HealthStatus;
import com.carebridge.backend.healthStatus.model.HealthStatusDto;
import org.springframework.stereotype.Component;

@Component
public class HealthStatusMapper {

    public HealthStatusDto toDto(HealthStatus status) {
        return new HealthStatusDto(
            status.getId(),
            status.getPainScale(),
            status.getMood(),
            status.getSymptoms(),
            status.getNotes(),
            status.getTimestamp(),
            status.getPatientId()
        );
    }

    public HealthStatus toEntity(HealthStatusDto dto) {
        return new HealthStatus(
            dto.id(),
            dto.painScale(),
            dto.mood(),
            dto.symptoms(),
            dto.notes(),
            dto.timestamp(),
            dto.patientId()
        );
    }
}
