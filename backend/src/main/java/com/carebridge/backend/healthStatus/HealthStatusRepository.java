package com.carebridge.backend.healthStatus;

import com.carebridge.backend.healthStatus.model.HealthStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class HealthStatusRepository {
    private final Map<UUID, HealthStatus> records = new ConcurrentHashMap<>();

    public Page<HealthStatus> findAll(Pageable pageable) {
        List<HealthStatus> allRecords = new ArrayList<>(records.values());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allRecords.size());

        List<HealthStatus> pageContent;
        if (start > allRecords.size()) {
            pageContent = new ArrayList<>();
        } else {
            pageContent = allRecords.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, allRecords.size());
    }

    public HealthStatus save(HealthStatus healthStatus) {
        if (healthStatus.getId() == null) {
            healthStatus.setId(UUID.randomUUID());
        }
        records.put(healthStatus.getId(), healthStatus);
        return healthStatus;
    }

    public Optional<HealthStatus> findById(UUID id) {
        return Optional.ofNullable(records.get(id));
    }

    public List<HealthStatus> findByPatientId(UUID patientId) {
        return records.values().stream()
                .filter(h -> h.getPatientId().equals(patientId))
                .toList();
    }

    public void deleteById(UUID id) {
        records.remove(id);
    }
}
