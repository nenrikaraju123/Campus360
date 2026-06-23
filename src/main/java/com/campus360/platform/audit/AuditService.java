package com.campus360.platform.audit;

import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.security.JwtAuthFilter.AuthPrincipal;
import com.campus360.platform.tenancy.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Enterprise audit service. Logs security-sensitive and business-critical
 * mutations to a persistent audit table for compliance, forensics, and
 * regulatory requirements.
 *
 * Usage: {@code auditService.log("GRADE_UPDATED", "Enrollment", id, "Score changed from 75 to 82")}
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Records an audit entry. Runs in its own transaction so that audit writes
     * succeed even if the outer business transaction rolls back (we want to
     * know about failed attempts too).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, Long entityId, String detail) {
        AuditLogEntry entry = buildEntry(action, entityType, entityId, detail);
        repository.save(entry);
        log.debug("Audit: {} {} #{} by {}", action, entityType, entityId, entry.getActorEmail());
    }

    /** Fire-and-forget variant for non-critical audit entries. */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAsync(String action, String entityType, Long entityId, String detail) {
        AuditLogEntry entry = buildEntry(action, entityType, entityId, detail);
        repository.save(entry);
    }

    private AuditLogEntry buildEntry(String action, String entityType, Long entityId, String detail) {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setTenantId(TenantContext.getTenantId());
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setDetail(detail);

        AuthPrincipal principal = CurrentUser.principal();
        if (principal != null) {
            entry.setActorId(principal.userId());
            entry.setActorEmail(principal.email());
        }

        entry.setIpAddress(resolveIp());
        return entry;
    }

    private String resolveIp() {
        try {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes sra) {
                HttpServletRequest req = sra.getRequest();
                String forwarded = req.getHeader("X-Forwarded-For");
                return forwarded != null ? forwarded.split(",")[0].trim() : req.getRemoteAddr();
            }
        } catch (Exception ignored) {
            // outside a request context (e.g. scheduler); IP is not critical
        }
        return null;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AuditLogEntry> searchAuditLogs(
            Long tenantId, Long actorId, String entityType, String action, 
            java.time.Instant fromDate, java.time.Instant toDate, 
            org.springframework.data.domain.Pageable pageable) {
        
        org.springframework.data.jpa.domain.Specification<AuditLogEntry> spec = org.springframework.data.jpa.domain.Specification.where(null);
        
        if (tenantId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tenantId"), tenantId));
        }
        if (actorId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("actorId"), actorId));
        }
        if (entityType != null && !entityType.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityType"), entityType));
        }
        if (action != null && !action.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("action"), action));
        }
        if (fromDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
        }
        if (toDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
        }
        
        return repository.findAll(spec, pageable);
    }
}
