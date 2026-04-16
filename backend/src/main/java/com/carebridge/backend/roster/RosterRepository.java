package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.Roster;
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
public class RosterRepository {
    private final Map<UUID, Roster> records = new ConcurrentHashMap<>();

    public Page<Roster> findAll(Pageable pageable) {
        List<Roster> allRecords = new ArrayList<>(records.values());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allRecords.size());

        List<Roster> pageContent;
        if (start > allRecords.size()) {
            pageContent = new ArrayList<>();
        } else {
            pageContent = allRecords.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, allRecords.size());
    }

    public Roster save(Roster roster) {
        if (roster.getId() == null) {
            roster.setId(UUID.randomUUID());
        }
        records.put(roster.getId(), roster);
        return roster;
    }

    public Optional<Roster> findById(UUID id) {
        return Optional.ofNullable(records.get(id));
    }

    public List<Roster> findByNurseId(UUID nurseId) {
        return records.values().stream()
                .filter(r -> r.getNurseId().equals(nurseId))
                .toList();
    }

    public void deleteById(UUID id) {
        records.remove(id);
    }
}
