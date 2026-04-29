package com.carebridge.backend.healthStatus;

import com.carebridge.backend.healthStatus.model.HealthStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HealthStatusRepository extends JpaRepository<HealthStatus, UUID> {
    List<HealthStatus> findByPatientId(UUID patientId);
}
