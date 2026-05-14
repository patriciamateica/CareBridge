package com.carebridge.backend.audit;

import com.carebridge.backend.audit.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Phase 9: AuditLogService handles business logic for audit logs.
 * This service provides high-level operations for querying and managing audit logs.
 */
@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Get all audit logs with pagination.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Get audit logs for a specific user.
     */
    @Transactional(readOnly = true)
    public long getActionCountForUserInTimeRange(UUID userId, LocalDateTime since) {
        return auditLogRepository.countByUserIdAndTimestampAfter(userId, since);
    }

    /**
     * Get failed action count for a specific user in a time range.
     * Useful for detecting brute force attempts.
     */
    @Transactional(readOnly = true)
    public long getFailureCountForUserInTimeRange(UUID userId, LocalDateTime since) {
        return auditLogRepository.countByUserIdAndStatusAndTimestampAfter(userId, "FAILURE", since);
    }

    /**
     * Get mutation (create/update/delete) count for a specific user in a time range.
     * Useful for detecting mass data modification attempts.
     */
    @Transactional(readOnly = true)
    public long getMutationCountForUserInTimeRange(UUID userId, LocalDateTime since) {
        return auditLogRepository.countMutations(userId, since, "Delete", "Update");
    }

    /**
     * Get a specific audit log by ID.
     */
    @Transactional(readOnly = true)
    public AuditLog getLogById(UUID id) {
        return auditLogRepository.findById(id).orElse(null);
    }

    /**
     * Create and save a new audit log entry.
     * Note: This is typically called automatically by the AuditAspect.
     */
    public AuditLog createLog(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    /**
     * Delete an audit log entry (for data retention compliance).
     */
    public void deleteLog(UUID id) {
        auditLogRepository.deleteById(id);
    }

    /**
     * Delete all audit logs older than a specified date.
     * Useful for data retention policies.
     */
    public void deleteLogsOlderThan(LocalDateTime date) {
        List<AuditLog> oldLogs = auditLogRepository.findAll().stream()
            .filter(log -> log.getTimestamp().isBefore(date))
            .toList();
        auditLogRepository.deleteAll(oldLogs);
    }
}

