package com.carebridge.backend.patientDetails;

import com.carebridge.backend.patientDetails.model.PatientDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PatientDetailsService {
    private final PatientDetailsRepository repository;

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
        return repository.save(patientDetails);
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
}
