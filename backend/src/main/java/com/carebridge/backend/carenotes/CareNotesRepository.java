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
        List<CareNotes> allRecords = new ArrayList<>(records.values());

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

    public void deleteById(UUID id) {
        records.remove(id);
    }
}
