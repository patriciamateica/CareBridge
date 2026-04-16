package com.carebridge.backend.patientDetails;

import com.carebridge.backend.patientDetails.model.PatientDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;

@Service
public class PatientDetailsService {
    private final PatientDetailsRepository repository;
    private final Sinks.Many<PatientDetails> createdSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<PatientDetails> diagnosisSink = Sinks.many().multicast().onBackpressureBuffer();

    public PatientDetailsService(PatientDetailsRepository repository) {
        this.repository = repository;
    }

    public Page<PatientDetails> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public PatientDetails getById(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    public PatientDetails create(PatientDetails patientDetails) {
        PatientDetails saved = repository.save(patientDetails);
        createdSink.tryEmitNext(saved);
        return saved;
    }

    public PatientDetails update(UUID id, PatientDetails updatedDetails) {
        PatientDetails oldDetails = repository.findById(id).orElseThrow();

        oldDetails.setUserId(updatedDetails.getUserId());
        oldDetails.setPrimaryDiagnosis(updatedDetails.getPrimaryDiagnosis());
        oldDetails.setDiagnostics(updatedDetails.getDiagnostics());
        oldDetails.setScans(updatedDetails.getScans());
        oldDetails.setEmergencyContact(updatedDetails.getEmergencyContact());
        oldDetails.setAssignedNurseId(updatedDetails.getAssignedNurseId());

        return oldDetails;
    }

    public boolean delete(UUID id) {
        repository.findById(id).orElseThrow();
        repository.deleteById(id);
        return true;
    }

    public PatientDetails getByUserId(UUID userId) {
        return repository.findByUserId(userId).orElseThrow();
    }

    public PatientDetails updateDiagnosis(UUID id, String primaryDiagnosis) {
        PatientDetails details = repository.findById(id).orElseThrow();
        details.setPrimaryDiagnosis(primaryDiagnosis);
        diagnosisSink.tryEmitNext(details);
        return details;
    }

    public Flux<PatientDetails> getCreatedStream(UUID userId) {
        return createdSink.asFlux()
                .filter(pd -> userId == null || pd.getUserId().equals(userId));
    }

    public Flux<PatientDetails> getDiagnosisStream(UUID id) {
        return diagnosisSink.asFlux()
                .filter(pd -> id == null || pd.getId().equals(id));
    }
}
