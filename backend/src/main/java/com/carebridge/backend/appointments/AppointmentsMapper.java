package com.carebridge.backend.appointments;

import com.carebridge.backend.appointments.model.Appointments;
import com.carebridge.backend.appointments.model.AppointmentsDto;
import org.springframework.stereotype.Component;

@Component
public class AppointmentsMapper {

    public AppointmentsDto toDto(Appointments appointments) {
        return new AppointmentsDto(
            appointments.getId(),
            appointments.getPatientId(),
            appointments.getNurseId(),
            appointments.getDescription(),
            appointments.getTimeSlot(),
            appointments.getStatus()
        );
    }

    public Appointments toEntity(AppointmentsDto dto) {
        return new Appointments(
            dto.id(),
            dto.patientId(),
            dto.nurseId(),
            dto.description(),
            dto.timeSlot(),
            dto.status()
        );
    }
}
