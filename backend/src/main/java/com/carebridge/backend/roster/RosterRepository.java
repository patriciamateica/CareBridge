package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.Roster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RosterRepository extends JpaRepository<Roster, UUID> {
    List<Roster> findByNurseId(UUID nurseId);
    Page<Roster> findByNurseIdOrderByIdDesc(UUID nurseId, Pageable pageable);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM roster WHERE nurse_id = ?1", nativeQuery = true)
    void deleteByNurseId(UUID nurseId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM roster WHERE patient_id = ?1", nativeQuery = true)
    void deleteByPatientId(UUID patientId);
}
