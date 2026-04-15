package com.carebridge.backend.appointments;

import com.carebridge.backend.appointments.model.Appointments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AppointmentsService {
    private final AppointmentsRepository appointmentsRepository;

    public AppointmentsService(AppointmentsRepository appointmentsRepository) {
        this.appointmentsRepository = appointmentsRepository;
    }

    public Page<Appointments> findAll(Pageable pageable) {
        return appointmentsRepository.findAll(pageable);
    }

    public Appointments getById(UUID id) {
        return appointmentsRepository.findById(id).orElseThrow();
    }

    public Appointments create(Appointments appointments) {
        return appointmentsRepository.save(appointments);
    }

    public Appointments update(UUID id, Appointments updatedAppointments) {
        Appointments oldAppointments = appointmentsRepository.findById(id).orElseThrow();

        oldAppointments.setPatientId(updatedAppointments.getPatientId());
        oldAppointments.setNurseId(updatedAppointments.getNurseId());
        oldAppointments.setDescription(updatedAppointments.getDescription());
        oldAppointments.setTimeSlot(updatedAppointments.getTimeSlot());
        oldAppointments.setStatus(updatedAppointments.getStatus());

        return oldAppointments;
    }

    public boolean delete(UUID id) {
        appointmentsRepository.findById(id).orElseThrow();
        appointmentsRepository.deleteById(id);
        return true;
    }
}
