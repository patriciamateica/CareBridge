package com.carebridge.backend.prescription;

import com.carebridge.backend.prescription.model.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PrescriptionRepository {
    private final Map<UUID, Prescription> records = new ConcurrentHashMap<>();

    public Page<Prescription> findAll(Pageable pageable) {
        List<Prescription> allRecords = sortByIdAscending(new ArrayList<>(records.values()));

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allRecords.size());

        List<Prescription> pageContent;
        if (start > allRecords.size()) {
            pageContent = new ArrayList<>();
        } else {
            pageContent = allRecords.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, allRecords.size());
    }

    public Prescription save(Prescription prescription) {
        if (prescription.getId() == null) {
            prescription.setId(UUID.randomUUID());
        }
        records.put(prescription.getId(), prescription);
        return prescription;
    }

    public Optional<Prescription> findById(UUID id) {
        return Optional.ofNullable(records.get(id));
    }

    public Page<Prescription> findByPatientId(UUID patientId, Pageable pageable) {
        List<Prescription> allRecords = sortByIdAscending(records.values().stream()
                .filter(p -> p.getPatientId().equals(patientId))
                .toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allRecords.size());

        List<Prescription> pageContent;
        if (start > allRecords.size()) {
            pageContent = new ArrayList<>();
        } else {
            pageContent = allRecords.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, allRecords.size());
    }

    public List<Prescription> findByPatientId(UUID patientId) {
        return sortByIdAscending(records.values().stream()
                .filter(p -> p.getPatientId().equals(patientId))
                .toList());
    }

    public long countByPatientId(UUID patientId) {
        return records.values().stream()
                .filter(p -> p.getPatientId().equals(patientId))
                .count();
    }

    public void deleteById(UUID id) {
        records.remove(id);
    }

    public void deleteAll() {
        records.clear();
    }

    private List<Prescription> sortByIdAscending(List<Prescription> input) {
        input.sort((a, b) -> {
            if (a.getId() == null && b.getId() == null) return 0;
            if (a.getId() == null) return 1;
            if (b.getId() == null) return -1;
            return a.getId().toString().compareTo(b.getId().toString());
        });
        return input;
    }
}
