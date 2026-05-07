package com.carebridge.backend.patientDetails;

import com.carebridge.backend.patientDetails.model.PatientCreateDto;
import com.carebridge.backend.patientDetails.model.PatientDetails;
import com.carebridge.backend.roster.RosterRepository;
import com.carebridge.backend.user.UserRepository;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;

@Service
@Transactional
public class PatientDetailsService {
    private final PatientDetailsRepository repository;
    private final UserRepository userRepository;
    private final RosterRepository rosterRepository;
    private final Sinks.Many<PatientDetails> createdSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<PatientDetails> diagnosisSink = Sinks.many().multicast().onBackpressureBuffer();

    public PatientDetailsService(PatientDetailsRepository repository, UserRepository userRepository, RosterRepository rosterRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.rosterRepository = rosterRepository;
    }

    @Transactional(readOnly = true)
    public Page<PatientDetails> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public PatientDetails getById(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    public PatientDetails create(PatientDetails patientDetails) {
        if (patientDetails.getUser() != null) {
            UUID userId = patientDetails.getUser().getId();

            return repository.findByUserId(userId)
                .map(existing -> {
                    existing.setPrimaryDiagnosis(patientDetails.getPrimaryDiagnosis());
                    existing.setDiagnostics(patientDetails.getDiagnostics());
                    existing.setScans(patientDetails.getScans());
                    existing.setEmergencyContact(patientDetails.getEmergencyContact());
                    existing.setAssignedNurseId(patientDetails.getAssignedNurseId());
                    existing.setStatus(patientDetails.getStatus());
                    return repository.save(existing);
                })
                .orElseGet(() -> {
                    User user = patientDetails.getUser();
                    if (user != null) {
                        user.setPatientDetails(patientDetails);
                    }
                    PatientDetails saved = repository.saveAndFlush(patientDetails);
                    createdSink.tryEmitNext(saved);
                    return saved;
                });
        }

        PatientDetails saved = repository.saveAndFlush(patientDetails);
        createdSink.tryEmitNext(saved);
        return saved;
    }

    public PatientDetails createPatient(PatientCreateDto dto) {
        User user = userRepository.findByEmail(dto.userEmail())
            .orElseThrow(() -> new IllegalArgumentException("The patient cannot be added as the email is not in the system yet."));

        if (!user.isActive()) {
            throw new IllegalArgumentException("This user account is deactivated.");
        }

        if (repository.existsByUser(user)) {
            throw new IllegalStateException("A patient profile already exists for this email.");
        }

        PatientDetails patientDetails = new PatientDetails();
        patientDetails.setUser(user);
        patientDetails.setPrimaryDiagnosis(dto.primaryDiagnosis());
        patientDetails.setDiagnostics(dto.diagnostics());
        patientDetails.setScans(dto.scans());
        patientDetails.setEmergencyContact(dto.emergencyContact());
        patientDetails.setAssignedNurseId(dto.assignedNurseId());
        patientDetails.setStatus(dto.status());
        PatientDetails saved = repository.saveAndFlush(patientDetails);

        // Sync with Roster
        if (saved.getAssignedNurseId() != null) {
            updateRoster(saved.getUser(), saved.getAssignedNurseId());
        }

        createdSink.tryEmitNext(saved);
        return saved;
    }

    private void updateRoster(User patient, java.util.UUID nurseId) {
        User nurse = userRepository.findById(nurseId).orElse(null);
        if (nurse == null) return;

        // Check if roster entry exists
        java.util.List<com.carebridge.backend.roster.model.Roster> existing = rosterRepository.findAll().stream()
            .filter(r -> r.getPatient().getId().equals(patient.getId()))
            .toList();

        if (existing.isEmpty()) {
            com.carebridge.backend.roster.model.Roster roster = new com.carebridge.backend.roster.model.Roster();
            roster.setPatient(patient);
            roster.setNurse(nurse);
            roster.setStatus(com.carebridge.backend.roster.model.RosterStatus.ACTIVE);
            rosterRepository.save(roster);
        } else {
            com.carebridge.backend.roster.model.Roster roster = existing.get(0);
            roster.setNurse(nurse);
            rosterRepository.save(roster);
        }
    }

    public PatientDetails update(UUID id, PatientDetails updatedDetails) {
        PatientDetails oldDetails = repository.findById(id).orElseThrow();

        oldDetails.setUser(updatedDetails.getUser());
        oldDetails.setPrimaryDiagnosis(updatedDetails.getPrimaryDiagnosis());
        oldDetails.setDiagnostics(updatedDetails.getDiagnostics());
        oldDetails.setScans(updatedDetails.getScans());
        oldDetails.setEmergencyContact(updatedDetails.getEmergencyContact());
        oldDetails.setAssignedNurseId(updatedDetails.getAssignedNurseId());
        oldDetails.setStatus(updatedDetails.getStatus());

        PatientDetails saved = repository.save(oldDetails);

        // Sync with Roster
        if (saved.getAssignedNurseId() != null) {
            updateRoster(saved.getUser(), saved.getAssignedNurseId());
        }

        createdSink.tryEmitNext(saved);
        return saved;
    }

    public boolean delete(UUID id) {
        PatientDetails patientDetails = repository.findById(id).orElseThrow();
        User user = patientDetails.getUser();
        if (user != null) {
            user.setActive(false);
            userRepository.save(user);
        }
        repository.delete(patientDetails);
        return true;
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    @Transactional(readOnly = true)
    public PatientDetails getByUserId(UUID userId) {
        return repository.findByUserId(userId).orElseThrow();
    }

    public PatientDetails updateDiagnosis(UUID id, String primaryDiagnosis) {
        PatientDetails details = repository.findById(id).orElseThrow();
        details.setPrimaryDiagnosis(primaryDiagnosis);

        PatientDetails saved = repository.save(details);
        diagnosisSink.tryEmitNext(saved);
        return saved;
    }

    public Flux<PatientDetails> getCreatedStream(UUID userId) {
        return createdSink.asFlux()
            .filter(pd -> userId == null || pd.getUser().getId().equals(userId));
    }

    public Flux<PatientDetails> getDiagnosisStream(UUID id) {
        return diagnosisSink.asFlux()
            .filter(pd -> id == null || pd.getId().equals(id));
    }

    @Transactional
    public void unassignNurse(UUID nurseId) {
        repository.unassignNurse(nurseId);
    }
}
