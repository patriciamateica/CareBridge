package com.carebridge.backend.audit;

import com.carebridge.backend.audit.model.SuspiciousUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SuspiciousUserRepository extends JpaRepository<SuspiciousUser, UUID> {
    Optional<SuspiciousUser> findByUserIdAndResolvedFalse(UUID userId);
}
