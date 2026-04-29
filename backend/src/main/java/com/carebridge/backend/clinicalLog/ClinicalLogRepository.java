package com.carebridge.backend.clinicalLog;

import com.carebridge.backend.clinicalLog.model.ClinicalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClinicalLogRepository extends JpaRepository<ClinicalLog, UUID> {
    List<ClinicalLog> findByPatientId(UUID patientId);
}
