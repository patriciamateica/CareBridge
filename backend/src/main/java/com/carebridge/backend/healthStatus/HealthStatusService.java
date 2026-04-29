package com.carebridge.backend.healthStatus;

import com.carebridge.backend.healthStatus.model.HealthStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class HealthStatusService {
    private final HealthStatusRepository healthStatusRepository;
    private final Sinks.Many<HealthStatus> statusSink = Sinks.many().multicast().onBackpressureBuffer();

    public HealthStatusService(HealthStatusRepository healthStatusRepository) {
        this.healthStatusRepository = healthStatusRepository;
    }

    @Transactional(readOnly = true)
    public Page<HealthStatus> findAll(Pageable pageable) {
        return healthStatusRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public HealthStatus getById(UUID id) {
        return healthStatusRepository.findById(id).orElseThrow();
    }

    @Transactional
    public HealthStatus create(HealthStatus healthStatus) {
        HealthStatus saved = healthStatusRepository.save(healthStatus);
        statusSink.tryEmitNext(saved);
        return saved;
    }

    @Transactional
    public HealthStatus update(UUID id, HealthStatus updatedStatus) {
        HealthStatus oldStatus = healthStatusRepository.findById(id).orElseThrow();

        oldStatus.setPainScale(updatedStatus.getPainScale());
        oldStatus.setMood(updatedStatus.getMood());
        oldStatus.setSymptoms(updatedStatus.getSymptoms());
        oldStatus.setNotes(updatedStatus.getNotes());

        HealthStatus saved = healthStatusRepository.save(oldStatus);
        statusSink.tryEmitNext(saved);
        return saved;
    }

    @Transactional
    public boolean delete(UUID id) {
        healthStatusRepository.findById(id).orElseThrow();
        healthStatusRepository.deleteById(id);
        return true;
    }

    @Transactional
    public void deleteAll() {
        healthStatusRepository.deleteAll();
    }

    @Transactional(readOnly = true)
    public List<HealthStatus> getByPatientId(UUID patientId) {
        return healthStatusRepository.findByPatientId(patientId);
    }

    @Transactional
    public Flux<HealthStatus> getStatusStream(UUID patientId) {
        return statusSink.asFlux()
            .filter(h -> patientId == null || h.getPatient().getId().equals(patientId));
    }
}
