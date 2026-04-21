package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotes;
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
public class CareNotesRepository {
    private final Map<UUID, CareNotes> records = new ConcurrentHashMap<>();

    public Page<CareNotes> findAll(Pageable pageable) {
        List<CareNotes> allRecords = sortByTimestampDescending(new ArrayList<>(records.values()));

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allRecords.size());

        List<CareNotes> pageContent;
        if (start > allRecords.size()) {
            pageContent = new ArrayList<>();
        } else {
            pageContent = allRecords.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, allRecords.size());
    }

    public CareNotes save(CareNotes careNotes) {
        if (careNotes.getId() == null) {
            careNotes.getId();
            careNotes.setId(UUID.randomUUID());
        }
        records.put(careNotes.getId(), careNotes);
        return careNotes;
    }

    public Optional<CareNotes> findById(UUID id) {
        return Optional.ofNullable(records.get(id));
    }

    public Page<CareNotes> findByPatientId(UUID patientId, Pageable pageable) {
        List<CareNotes> allRecords = sortByTimestampDescending(new ArrayList<>(records.values().stream()
                .filter(n -> n.getPatientId().equals(patientId))
                .toList()));

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allRecords.size());

        List<CareNotes> pageContent;
        if (start > allRecords.size()) {
            pageContent = new ArrayList<>();
        } else {
            pageContent = allRecords.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, allRecords.size());
    }

    public List<CareNotes> findByPatientId(UUID patientId) {
        return sortByTimestampDescending(new ArrayList<>(records.values().stream()
                .filter(n -> n.getPatientId().equals(patientId))
                .toList()));
    }

    public long countByPatientId(UUID patientId) {
        return records.values().stream()
                .filter(n -> n.getPatientId().equals(patientId))
                .count();
    }

    public void deleteById(UUID id) {
        records.remove(id);
    }

    public void deleteAll() {
        records.clear();
    }

    private List<CareNotes> sortByTimestampDescending(List<CareNotes> input) {
        input.sort((a, b) -> {
            if (a.getTimestamp() == null && b.getTimestamp() == null) return 0;
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });
        return input;
    }
}
