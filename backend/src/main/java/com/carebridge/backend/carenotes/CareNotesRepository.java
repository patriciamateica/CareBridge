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
    Page<CareNotes> findByPatientIdOrderByTimestampDesc(UUID patientId, Pageable pageable);
    List<CareNotes> findByPatientIdOrderByTimestampDesc(UUID patientId);
    long countByPatientId(UUID patientId);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM carenotes WHERE patient_id = ?1", nativeQuery = true)
    void deleteByPatientId(UUID patientId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM carenotes WHERE nurse_id = ?1", nativeQuery = true)
    void deleteByNurseId(UUID nurseId);
}
