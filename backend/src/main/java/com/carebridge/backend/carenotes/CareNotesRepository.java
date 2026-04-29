package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CareNotesRepository extends JpaRepository<CareNotes, UUID> {
    Page<CareNotes> findByPatientId(UUID patientId, Pageable pageable);
    List<CareNotes> findByPatientIdOrderByTimestampDesc(UUID patientId);
    long countByPatientId(UUID patientId);
}
