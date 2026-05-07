package com.carebridge.backend.audit;

import com.carebridge.backend.audit.model.AuditLog;
import com.carebridge.backend.user.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final MalvolentBehaviorService malvolentBehaviorService;

    public AuditAspect(AuditLogRepository auditLogRepository, MalvolentBehaviorService malvolentBehaviorService) {
        this.auditLogRepository = auditLogRepository;
        this.malvolentBehaviorService = malvolentBehaviorService;
    }

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        LogAction logAction = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(LogAction.class);
        
        UUID userId = null;
        String userRole = "GUEST";
        String username = "ANONYMOUS";
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null ?
            SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;

        if (principal instanceof CustomUserDetails userDetails) {
            userId = userDetails.getUser().getId();
            username = userDetails.getUsername();
            userRole = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst().orElse("USER");
        }

        HttpServletRequest request = null;
        if (RequestContextHolder.getRequestAttributes() != null) {
            request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        }

        String action = (logAction != null && !logAction.value().isEmpty()) ? logAction.value() : joinPoint.getSignature().getName();
        String resource = request != null ? request.getRequestURI() : joinPoint.getSignature().getDeclaringTypeName();
        String ipAddress = request != null ? request.getRemoteAddr() : "0.0.0.0";

        AuditLog.AuditLogBuilder logBuilder = AuditLog.builder()
            .userId(userId)
            .userRole(userRole)
            .username(username)
            .action(action)
            .resource(resource)
            .ipAddress(ipAddress)
            .timestamp(LocalDateTime.now());

        try {
            Object result = joinPoint.proceed();
            logBuilder.status("SUCCESS");
            AuditLog log = auditLogRepository.save(logBuilder.build());
            malvolentBehaviorService.analyzeBehavior(log);
            return result;
        } catch (Throwable throwable) {
            logBuilder.status("FAILURE");
            logBuilder.details(throwable.getMessage());
            AuditLog log = auditLogRepository.save(logBuilder.build());
            malvolentBehaviorService.analyzeBehavior(log);
            throw throwable;
        }
    }
}
