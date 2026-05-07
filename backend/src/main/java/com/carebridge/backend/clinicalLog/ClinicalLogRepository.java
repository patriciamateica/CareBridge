package com.carebridge.backend.clinicalLog;

import com.carebridge.backend.clinicalLog.model.ClinicalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClinicalLogRepository extends JpaRepository<ClinicalLog, UUID> {
    List<ClinicalLog> findByPatientId(UUID patientId);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM clinical_logs WHERE patient_id = ?1", nativeQuery = true)
    void deleteByPatientId(UUID patientId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM clinical_logs WHERE nurse_id = ?1", nativeQuery = true)
    void deleteByNurseId(UUID nurseId);
}
