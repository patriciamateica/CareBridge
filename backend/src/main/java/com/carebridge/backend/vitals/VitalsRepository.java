package com.carebridge.backend.vitals;

import com.carebridge.backend.vitals.model.Vitals;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class VitalsRepository {
    private final Map<UUID, Vitals> vitals = new ConcurrentHashMap<>();

    public Page<Vitals> findAll(Pageable pageable) {
        List<Vitals> allVitals = new ArrayList<>(vitals.values());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allVitals.size());

        List<Vitals> pageContent;
        if (start > allVitals.size()) {
            pageContent = new ArrayList<>();
        } else {
            pageContent = allVitals.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, allVitals.size());
    }

    public Vitals save(Vitals vitals) {
        if (vitals.getId() == null) {
            vitals.setId(UUID.randomUUID());
        }
        this.vitals.put(vitals.getId(), vitals);
        return vitals;
    }

    public Optional<Vitals> findById(UUID id) {
        return Optional.ofNullable(vitals.get(id));
    }

    public void deleteById(UUID id) {
        vitals.remove(id);
    }
}
