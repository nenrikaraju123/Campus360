package com.campus360.platform.audit;

import com.campus360.platform.tenancy.TenantContext;
import com.campus360.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit-log")
@PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','SUPER_ADMIN')")
@Tag(name = "Audit Log", description = "Enterprise audit trail (read-only)")
public class AuditLogController {

    private final AuditLogRepository repository;
    private final AuditService auditService;

    public AuditLogController(AuditLogRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @GetMapping
    @Operation(summary = "Query audit log entries for the current tenant (paginated)")
    public PageResponse<AuditLogEntry> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.Instant fromDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.Instant toDate) {
            
        var pageable = PageRequest.of(page, Math.min(size, 200), Sort.by("createdAt").descending());
        
        boolean isSuperAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        
        Long tenant = null;
        if (!isSuperAdmin) {
            tenant = TenantContext.requireTenantId();
        }

        return PageResponse.of(auditService.searchAuditLogs(tenant, actorId, entityType, action, fromDate, toDate, pageable));
    }
}
