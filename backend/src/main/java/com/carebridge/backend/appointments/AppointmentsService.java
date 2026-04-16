package com.carebridge.backend.appointments;

import com.carebridge.backend.appointments.model.Appointments;
import com.carebridge.backend.appointments.model.AppointmentsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.UUID;

@Service
public class AppointmentsService {
    private final AppointmentsRepository appointmentsRepository;
    private final Sinks.Many<Appointments> scheduledSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<Appointments> statusChangedSink = Sinks.many().multicast().onBackpressureBuffer();

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
        Appointments saved = appointmentsRepository.save(appointments);
        scheduledSink.tryEmitNext(saved);
        return saved;
    }

    public Appointments update(UUID id, Appointments updatedAppointments) {
        Appointments oldAppointments = appointmentsRepository.findById(id).orElseThrow();

        oldAppointments.setPatientId(updatedAppointments.getPatientId());
        oldAppointments.setNurseId(updatedAppointments.getNurseId());
        oldAppointments.setDescription(updatedAppointments.getDescription());
        oldAppointments.setTimeSlot(updatedAppointments.getTimeSlot());
        oldAppointments.setStatus(updatedAppointments.getStatus());

        scheduledSink.tryEmitNext(oldAppointments); // It's scheduled/updated
        return oldAppointments;
    }

    public Appointments updateStatus(UUID id, AppointmentsStatus status) {
        Appointments appointment = appointmentsRepository.findById(id).orElseThrow();
        appointment.setStatus(status);
        statusChangedSink.tryEmitNext(appointment);
        return appointment;
    }

    public boolean delete(UUID id) {
        appointmentsRepository.findById(id).orElseThrow();
        appointmentsRepository.deleteById(id);
        return true;
    }

    public List<Appointments> getByPatientId(UUID patientId) {
        return appointmentsRepository.findByPatientId(patientId);
    }

    public Flux<Appointments> getScheduledStream(UUID patientId, UUID nurseId) {
        return scheduledSink.asFlux()
                .filter(a -> (patientId == null || a.getPatientId().equals(patientId))
                        && (nurseId == null || a.getNurseId().equals(nurseId)));
    }

    public Flux<Appointments> getStatusChangedStream(UUID id) {
        return statusChangedSink.asFlux()
                .filter(a -> id == null || a.getId().equals(id));
    }
}
