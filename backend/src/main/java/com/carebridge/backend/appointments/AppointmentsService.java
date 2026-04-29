package com.carebridge.backend.appointments;

import com.carebridge.backend.appointments.model.Appointments;
import com.carebridge.backend.appointments.model.AppointmentsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional(readOnly = true)
    public Page<Appointments> findAll(Pageable pageable) {
        return appointmentsRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Appointments getById(UUID id) {
        return appointmentsRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Appointments create(Appointments appointments) {
        Appointments saved = appointmentsRepository.save(appointments);
        scheduledSink.tryEmitNext(saved);
        return saved;
    }

    @Transactional
    public Appointments update(UUID id, Appointments updatedAppointments) {
        Appointments oldAppointments = appointmentsRepository.findById(id).orElseThrow();

        oldAppointments.setPatient(updatedAppointments.getPatient());
        oldAppointments.setNurse(updatedAppointments.getNurse());
        oldAppointments.setDescription(updatedAppointments.getDescription());
        oldAppointments.setTimeSlot(updatedAppointments.getTimeSlot());
        oldAppointments.setStatus(updatedAppointments.getStatus());

        Appointments saved = appointmentsRepository.save(oldAppointments);
        scheduledSink.tryEmitNext(saved);
        return saved;
    }

    @Transactional
    public Appointments updateStatus(UUID id, AppointmentsStatus status) {
        Appointments appointment = appointmentsRepository.findById(id).orElseThrow();
        appointment.setStatus(status);

        Appointments saved = appointmentsRepository.save(appointment);
        statusChangedSink.tryEmitNext(saved);
        return saved;
    }

    @Transactional
    public boolean delete(UUID id) {
        appointmentsRepository.findById(id).orElseThrow();
        appointmentsRepository.deleteById(id);
        return true;
    }

    @Transactional
    public void deleteAll() {
        appointmentsRepository.deleteAll();
    }

    @Transactional(readOnly = true)
    public List<Appointments> getByPatientId(UUID patientId) {
        return appointmentsRepository.findByPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public Flux<Appointments> getScheduledStream(UUID patientId, UUID nurseId) {
        return scheduledSink.asFlux()
            .filter(a -> (patientId == null || a.getPatient().getId().equals(patientId))
                && (nurseId == null || a.getNurse().getId().equals(nurseId)));
    }

    @Transactional(readOnly = true)
    public Flux<Appointments> getStatusChangedStream(UUID id) {
        return statusChangedSink.asFlux()
            .filter(a -> id == null || a.getId().equals(id));
    }
}
