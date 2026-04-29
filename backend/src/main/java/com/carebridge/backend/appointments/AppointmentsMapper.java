package com.carebridge.backend.appointments;

import com.carebridge.backend.appointments.model.Appointments;
import com.carebridge.backend.appointments.model.AppointmentsDto;
import com.carebridge.backend.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class AppointmentsMapper {

    public AppointmentsDto toDto(Appointments appointments) {
        return new AppointmentsDto(
            appointments.getId(),
            appointments.getPatient().getId(),
            appointments.getNurse().getId(),
            appointments.getDescription(),
            appointments.getTimeSlot(),
            appointments.getStatus()
        );
    }

    public Appointments toEntity(AppointmentsDto dto, User nurse, User patient) {
        return new Appointments(
            dto.id(),
            patient,
            nurse,
            dto.description(),
            dto.timeSlot(),
            dto.status()
        );
    }
}
