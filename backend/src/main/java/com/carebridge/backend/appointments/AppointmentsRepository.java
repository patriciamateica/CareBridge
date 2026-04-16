package com.carebridge.backend.appointments;

import com.carebridge.backend.appointments.model.Appointments;
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
public class AppointmentsRepository {
    private final Map<UUID, Appointments> records = new ConcurrentHashMap<>();

    public Page<Appointments> findAll(Pageable pageable) {
        List<Appointments> allRecords = new ArrayList<>(records.values());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allRecords.size());

        List<Appointments> pageContent;
        if (start > allRecords.size()) {
            pageContent = new ArrayList<>();
        } else {
            pageContent = allRecords.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, allRecords.size());
    }

    public Appointments save(Appointments appointments) {
        if (appointments.getId() == null) {
            appointments.setId(UUID.randomUUID());
        }
        records.put(appointments.getId(), appointments);
        return appointments;
    }

    public Optional<Appointments> findById(UUID id) {
        return Optional.ofNullable(records.get(id));
    }

    public List<Appointments> findByPatientId(UUID patientId) {
        return records.values().stream()
                .filter(a -> a.getPatientId().equals(patientId))
                .toList();
    }

    public void deleteById(UUID id) {
        records.remove(id);
    }

    public void deleteAll() {
        records.clear();
    }
}
