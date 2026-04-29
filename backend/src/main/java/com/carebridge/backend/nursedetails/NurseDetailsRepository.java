package com.carebridge.backend.nursedetails;

import com.carebridge.backend.nursedetails.model.NurseDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NurseDetailsRepository extends JpaRepository<NurseDetails, UUID> {
    Optional<NurseDetails> findByUserId(UUID userId);
}
