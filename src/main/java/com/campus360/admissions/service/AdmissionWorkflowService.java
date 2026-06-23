package com.campus360.admissions.service;

import com.campus360.admissions.domain.AdmissionApplication;
import com.campus360.admissions.domain.AdmissionStatusHistory;
import com.campus360.admissions.repository.AdmissionApplicationRepository;
import com.campus360.admissions.repository.AdmissionStatusHistoryRepository;
import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.tenancy.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class AdmissionWorkflowService {

    /**
     * Defines legal transitions: fromStatus -> allowed toStatuses.
     * Map.ofEntries is used because we have more than 10 entries (Map.of is limited to 10).
     */
    private static final Map<String, Set<String>> TRANSITIONS = Map.ofEntries(
            Map.entry("APPLICATION_RECEIVED", Set.of("DOCUMENT_PENDING", "UNDER_REVIEW", "CANCELLED")),
            Map.entry("DOCUMENT_PENDING",     Set.of("UNDER_REVIEW", "CANCELLED")),
            Map.entry("UNDER_REVIEW",         Set.of("SHORTLISTED", "REJECTED", "CANCELLED")),
            Map.entry("SHORTLISTED",          Set.of("INTERVIEW_SCHEDULED", "APPROVED", "REJECTED", "WAITLISTED")),
            Map.entry("INTERVIEW_SCHEDULED",  Set.of("APPROVED", "REJECTED", "WAITLISTED")),
            Map.entry("APPROVED",             Set.of("OFFERED", "CANCELLED")),
            Map.entry("WAITLISTED",           Set.of("APPROVED", "CANCELLED")),
            Map.entry("OFFERED",              Set.of("ENROLLED", "CANCELLED")),
            Map.entry("REJECTED",             Set.of()),
            Map.entry("ENROLLED",             Set.of()),
            Map.entry("CANCELLED",            Set.of())
    );

    private final AdmissionApplicationRepository applicationRepository;
    private final AdmissionStatusHistoryRepository historyRepository;
    private final AuditService auditService;

    public AdmissionWorkflowService(AdmissionApplicationRepository applicationRepository,
                                    AdmissionStatusHistoryRepository historyRepository,
                                    AuditService auditService) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
        this.auditService = auditService;
    }

    public AdmissionApplication transition(Long applicationId, String targetStatus, String comment) {
        Long tenantId = TenantContext.requireTenantId();
        AdmissionApplication app = applicationRepository.findByIdAndTenantId(applicationId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Application not found: " + applicationId));

        String fromStatus = app.getStatus();
        Set<String> allowed = TRANSITIONS.getOrDefault(fromStatus, Set.of());
        if (!allowed.contains(targetStatus)) {
            throw ApiException.badRequest(
                    String.format("Cannot transition from '%s' to '%s'", fromStatus, targetStatus));
        }

        app.setStatus(targetStatus);
        app.setUpdatedAt(Instant.now());
        applicationRepository.save(app);

        // Write status history
        AdmissionStatusHistory history = new AdmissionStatusHistory();
        history.setTenantId(tenantId);
        history.setApplicationId(applicationId);
        history.setFromStatus(fromStatus);
        history.setToStatus(targetStatus);
        history.setComment(comment);
        history.setActorId(CurrentUser.principal() != null ? CurrentUser.principal().userId() : null);
        historyRepository.save(history);

        // Write audit log
        auditService.log("ADMISSION_STATUS_CHANGED", "AdmissionApplication", applicationId,
                String.format("%s → %s: %s", fromStatus, targetStatus, comment != null ? comment : ""));

        return app;
    }

    @Transactional(readOnly = true)
    public List<AdmissionStatusHistory> getStatusHistory(Long applicationId) {
        Long tenantId = TenantContext.requireTenantId();
        // validate application belongs to tenant
        applicationRepository.findByIdAndTenantId(applicationId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Application not found: " + applicationId));
        return historyRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId);
    }
}
