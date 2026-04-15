package com.carebridge.backend.nursedetails;

import com.carebridge.backend.nursedetails.model.NurseDetails;
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
public class NurseDetailsRepository {
    private final Map<UUID, NurseDetails> records = new ConcurrentHashMap<>();

    public Page<NurseDetails> findAll(Pageable pageable) {
        List<NurseDetails> allRecords = new ArrayList<>(records.values());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allRecords.size());

        List<NurseDetails> pageContent;
        if (start > allRecords.size()) {
            pageContent = new ArrayList<>();
        } else {
            pageContent = allRecords.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, allRecords.size());
    }

    public NurseDetails save(NurseDetails nurseDetails) {
        if (nurseDetails.getId() == null) {
            nurseDetails.setId(UUID.randomUUID());
        }
        records.put(nurseDetails.getId(), nurseDetails);
        return nurseDetails;
    }

    public Optional<NurseDetails> findById(UUID id) {
        return Optional.ofNullable(records.get(id));
    }

    public void deleteById(UUID id) {
        records.remove(id);
    }
}
