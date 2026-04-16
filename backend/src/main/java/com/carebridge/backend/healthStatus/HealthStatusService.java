package com.carebridge.backend.healthStatus;

import com.carebridge.backend.healthStatus.model.HealthStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.UUID;

@Service
public class HealthStatusService {
    private final HealthStatusRepository healthStatusRepository;
    private final Sinks.Many<HealthStatus> statusSink = Sinks.many().multicast().onBackpressureBuffer();

    public HealthStatusService(HealthStatusRepository healthStatusRepository) {
        this.healthStatusRepository = healthStatusRepository;
    }

    public Page<HealthStatus> findAll(Pageable pageable) {
        return healthStatusRepository.findAll(pageable);
    }

    public HealthStatus getById(UUID id) {
        return healthStatusRepository.findById(id).orElseThrow();
    }

    public HealthStatus create(HealthStatus healthStatus) {
        HealthStatus saved = healthStatusRepository.save(healthStatus);
        statusSink.tryEmitNext(saved);
        return saved;
    }

    public HealthStatus update(UUID id, HealthStatus updatedStatus) {
        HealthStatus oldStatus = healthStatusRepository.findById(id).orElseThrow();

        oldStatus.setPainScale(updatedStatus.getPainScale());
        oldStatus.setMood(updatedStatus.getMood());
        oldStatus.setSymptoms(updatedStatus.getSymptoms());
        oldStatus.setNotes(updatedStatus.getNotes());

        return oldStatus;
    }

    public boolean delete(UUID id) {
        healthStatusRepository.findById(id).orElseThrow(); // Ensure it exists
        healthStatusRepository.deleteById(id);
        return true;
    }

    public void deleteAll() {
        healthStatusRepository.deleteAll();
    }

    public List<HealthStatus> getByPatientId(UUID patientId) {
        return healthStatusRepository.findByPatientId(patientId);
    }

    public Flux<HealthStatus> getStatusStream(UUID patientId) {
        return statusSink.asFlux()
                .filter(h -> patientId == null || h.getPatientId().equals(patientId));
    }
}
