package com.carebridge.backend.patientDetails;

import com.carebridge.backend.patientDetails.model.PatientDetails;
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
public class PatientDetailsRepository {
    private final Map<UUID, PatientDetails> records = new ConcurrentHashMap<>();

    public Page<PatientDetails> findAll(Pageable pageable) {
        List<PatientDetails> allRecords = new ArrayList<>(records.values());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allRecords.size());

        List<PatientDetails> pageContent;
        if (start > allRecords.size()) {
            pageContent = new ArrayList<>();
        } else {
            pageContent = allRecords.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, allRecords.size());
    }

    public PatientDetails save(PatientDetails patientDetails) {
        if (patientDetails.getId() == null) {
            patientDetails.setId(UUID.randomUUID());
        }
        records.put(patientDetails.getId(), patientDetails);
        return patientDetails;
    }

    public Optional<PatientDetails> findById(UUID id) {
        return Optional.ofNullable(records.get(id));
    }

    public Optional<PatientDetails> findByUserId(UUID userId) {
        return records.values().stream()
                .filter(pd -> pd.getUserId().equals(userId))
                .findFirst();
    }

    public void deleteById(UUID id) {
        records.remove(id);
    }
}
