package com.carebridge.backend.clinicalLog;

import com.carebridge.backend.clinicalLog.model.ClinicalLog;
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
public class ClinicalLogRepository {
    private final Map<UUID, ClinicalLog> records = new ConcurrentHashMap<>();

    public Page<ClinicalLog> findAll(Pageable pageable) {
        List<ClinicalLog> allRecords = new ArrayList<>(records.values());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allRecords.size());

        List<ClinicalLog> pageContent;
        if (start > allRecords.size()) {
            pageContent = new ArrayList<>();
        } else {
            pageContent = allRecords.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, allRecords.size());
    }

    public ClinicalLog save(ClinicalLog clinicalLog) {
        if (clinicalLog.getId() == null) {
            clinicalLog.setId(UUID.randomUUID());
        }
        records.put(clinicalLog.getId(), clinicalLog);
        return clinicalLog;
    }

    public Optional<ClinicalLog> findById(UUID id) {
        return Optional.ofNullable(records.get(id));
    }

    public void deleteById(UUID id) {
        records.remove(id);
    }
}
