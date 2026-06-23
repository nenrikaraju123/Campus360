package com.campus360.exams.service;

import com.campus360.exams.domain.ExamMarkSheet;
import com.campus360.exams.domain.ResultStatusHistory;
import com.campus360.exams.repository.ExamMarkSheetRepository;
import com.campus360.exams.repository.ResultStatusHistoryRepository;
import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.tenancy.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class ExamWorkflowService {

    private final ExamMarkSheetRepository markSheetRepo;
    private final ResultStatusHistoryRepository historyRepo;
    private final AuditService auditService;

    public ExamWorkflowService(ExamMarkSheetRepository markSheetRepo,
                               ResultStatusHistoryRepository historyRepo,
                               AuditService auditService) {
        this.markSheetRepo = markSheetRepo;
        this.historyRepo = historyRepo;
        this.auditService = auditService;
    }

    public ExamMarkSheet submitMarkSheet(Long markSheetId) {
        return transitionMarkSheet(markSheetId, "DRAFT", "SUBMITTED", "Submitted marks for review");
    }

    public ExamMarkSheet returnToDraft(Long markSheetId, String comments) {
        return transitionMarkSheet(markSheetId, "SUBMITTED", "DRAFT", "Returned: " + comments);
    }

    public ExamMarkSheet reviewMarkSheet(Long markSheetId) {
        ExamMarkSheet ms = transitionMarkSheet(markSheetId, "SUBMITTED", "REVIEWED", "Mark sheet reviewed");
        ms.setReviewedAt(Instant.now());
        if (CurrentUser.principal() != null) {
            ms.setReviewedBy(CurrentUser.principal().userId());
        }
        return markSheetRepo.save(ms);
    }

    public ExamMarkSheet publishMarkSheet(Long markSheetId) {
        return transitionMarkSheet(markSheetId, "REVIEWED", "PUBLISHED", "Mark sheet published");
    }

    private ExamMarkSheet transitionMarkSheet(Long markSheetId, String expectedState, String newState, String comments) {
        Long tenantId = TenantContext.requireTenantId();
        ExamMarkSheet ms = markSheetRepo.findByIdAndTenantId(markSheetId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Mark sheet not found"));

        if (!ms.getStatus().equals(expectedState)) {
            throw ApiException.badRequest("Cannot transition mark sheet from " + ms.getStatus() + " to " + newState);
        }

        String oldStatus = ms.getStatus();
        ms.setStatus(newState);
        ms.setUpdatedAt(Instant.now());
        if (newState.equals("SUBMITTED")) {
            ms.setSubmittedAt(Instant.now());
        }
        ms = markSheetRepo.save(ms);

        // Record History
        ResultStatusHistory history = new ResultStatusHistory();
        history.setTenantId(tenantId);
        history.setEntityType("MARK_SHEET");
        history.setEntityId(ms.getId());
        history.setPreviousStatus(oldStatus);
        history.setNewStatus(newState);
        history.setChangedBy(CurrentUser.principal() != null ? CurrentUser.principal().email() : "system");
        history.setComments(comments);
        historyRepo.save(history);

        auditService.log("MARK_SHEET_TRANSITION", "ExamMarkSheet", ms.getId(),
                "Mark sheet changed from " + oldStatus + " to " + newState);
        return ms;
    }
}
