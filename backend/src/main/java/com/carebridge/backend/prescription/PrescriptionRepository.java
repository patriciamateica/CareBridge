package com.carebridge.backend.prescription;

import com.carebridge.backend.prescription.model.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    Page<Prescription> findByPatientId(UUID patientId, Pageable pageable);
    List<Prescription> findByPatientIdOrderByIdAsc(UUID patientId);
    long countByPatientId(UUID patientId);
}
