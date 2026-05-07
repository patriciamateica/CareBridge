package com.carebridge.backend.audit;

import com.carebridge.backend.audit.model.AuditLog;
import com.carebridge.backend.audit.model.SuspiciousUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MalvolentBehaviorService {

    private final AuditLogRepository auditLogRepository;
    private final SuspiciousUserRepository suspiciousUserRepository;

    public MalvolentBehaviorService(AuditLogRepository auditLogRepository, SuspiciousUserRepository suspiciousUserRepository) {
        this.auditLogRepository = auditLogRepository;
        this.suspiciousUserRepository = suspiciousUserRepository;
    }

    @Transactional
    public void analyzeBehavior(AuditLog latestLog) {
        if (latestLog.getUserId() == null) return;

        UUID userId = latestLog.getUserId();
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

        // Rule 1: High Frequency (Potential Bot)
        long actionCount = auditLogRepository.countByUserIdAndTimestampAfter(userId, fiveMinutesAgo);

        if (actionCount > 50) {
            flagUser(latestLog, "Extremely high activity detected (Bot-like)", "HIGH");
            return;
        }

        // Rule 2: Repeated Failures (Potential Brute Force or Exploit attempt)
        long failureCount = auditLogRepository.countByUserIdAndStatusAndTimestampAfter(userId, "FAILURE", fiveMinutesAgo);

        if (failureCount > 10) {
            flagUser(latestLog, "Multiple failed actions detected", "MEDIUM");
            return;
        }

        // Rule 3: Mass Deletion / Mutation spike
        long mutationCount = auditLogRepository.countMutations(userId, fiveMinutesAgo, "Delete", "Update");

        if (mutationCount > 20) {
            flagUser(latestLog, "Rapid mass data modification detected", "MEDIUM");
        }
    }

    private void flagUser(AuditLog log, String reason, String severity) {
        if (suspiciousUserRepository.findByUserIdAndResolvedFalse(log.getUserId()).isPresent()) {
            return;
        }

        SuspiciousUser suspiciousUser = SuspiciousUser.builder()
            .userId(log.getUserId())
            .username(log.getUsername())
            .reason(reason)
            .severity(severity)
            .flaggedAt(LocalDateTime.now())
            .resolved(false)
            .build();

        suspiciousUserRepository.save(suspiciousUser);
    }
}
