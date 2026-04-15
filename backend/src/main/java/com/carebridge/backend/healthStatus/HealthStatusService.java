package com.carebridge.backend.healthStatus;

import com.carebridge.backend.healthStatus.model.HealthStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HealthStatusService {
    private final HealthStatusRepository healthStatusRepository;

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
        return healthStatusRepository.save(healthStatus);
    }

    public HealthStatus update(UUID id, HealthStatus updatedStatus) {
        HealthStatus oldStatus = healthStatusRepository.findById(id).orElseThrow();

        oldStatus.setPainScale(updatedStatus.getPainScale());
        oldStatus.setMood(updatedStatus.getMood());
        oldStatus.setSymptoms(updatedStatus.getSymptoms());
        oldStatus.setNotes(updatedStatus.getNotes());

        return oldStatus; // Automatically updated in the RAM map since it's the same object reference
    }

    public boolean delete(UUID id) {
        healthStatusRepository.findById(id).orElseThrow(); // Ensure it exists
        healthStatusRepository.deleteById(id);
        return true;
    }
}
