package com.carebridge.backend.audit;

import com.carebridge.backend.audit.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditLogRepository auditLogRepository;
    private final SuspiciousUserRepository suspiciousUserRepository;

    public AuditController(AuditLogRepository auditLogRepository, SuspiciousUserRepository suspiciousUserRepository) {
        this.auditLogRepository = auditLogRepository;
        this.suspiciousUserRepository = suspiciousUserRepository;
    }

    @GetMapping
    @LogAction("View Audit Logs")
    public Page<AuditLog> getLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @GetMapping("/suspicious")
    @LogAction("View Suspicious Users")
    public java.util.List<com.carebridge.backend.audit.model.SuspiciousUser> getSuspiciousUsers() {
        return suspiciousUserRepository.findAll();
    }

    @org.springframework.web.bind.annotation.PostMapping("/suspicious/{id}/resolve")
    @LogAction("Resolve Suspicious User Flag")
    public void resolveSuspiciousUser(@org.springframework.web.bind.annotation.PathVariable java.util.UUID id) {
        suspiciousUserRepository.findById(id).ifPresent(u -> {
            u.setResolved(true);
            suspiciousUserRepository.save(u);
        });
    }
}
