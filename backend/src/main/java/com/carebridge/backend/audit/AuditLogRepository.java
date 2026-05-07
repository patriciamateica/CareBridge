package com.carebridge.backend.audit;

import com.carebridge.backend.audit.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    long countByUserIdAndTimestampAfter(UUID userId, LocalDateTime timestamp);

    long countByUserIdAndStatusAndTimestampAfter(UUID userId, String status, LocalDateTime timestamp);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(l) FROM AuditLog l WHERE l.userId = :userId " +
        "AND l.timestamp > :timestamp AND (l.action LIKE %:act1% OR l.action LIKE %:act2%)")
    long countMutations(@org.springframework.data.repository.query.Param("userId") UUID userId,
                        @org.springframework.data.repository.query.Param("timestamp") LocalDateTime timestamp,
                        @org.springframework.data.repository.query.Param("act1") String act1,
                        @org.springframework.data.repository.query.Param("act2") String act2);
}
