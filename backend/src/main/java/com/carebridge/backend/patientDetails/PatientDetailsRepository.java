package com.carebridge.backend.patientDetails;

import com.carebridge.backend.patientDetails.model.PatientDetails;
import com.carebridge.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientDetailsRepository extends JpaRepository<PatientDetails, UUID> {
    Optional<PatientDetails> findByUserId(UUID userId);
    boolean existsByUser(User user);

    @Modifying
    @Query("UPDATE PatientDetails p SET p.assignedNurseId = NULL WHERE p.assignedNurseId = :nurseId")
    void unassignNurse(UUID nurseId);
}
