package com.carebridge.backend.vitals;

import com.carebridge.backend.vitals.model.Vitals;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VitalsRepository extends JpaRepository<Vitals, UUID> {
    List<Vitals> findByPatientId(UUID patientId);
    Page<Vitals> findByPatientId(UUID patientId, Pageable pageable);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM vitals WHERE patient_id = ?1", nativeQuery = true)
    void deleteByPatientId(UUID patientId);
}
