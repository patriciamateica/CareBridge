package com.carebridge.backend.vitals;

import com.carebridge.backend.vitals.model.Vitals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VitalsRepository extends JpaRepository<Vitals, UUID> {
    List<Vitals> findByPatientId(UUID patientId);
}
