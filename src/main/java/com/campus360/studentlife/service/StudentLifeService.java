package com.campus360.studentlife.service;

import com.campus360.notification.domain.NotificationEvent;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.shared.dto.PageResponse;
import com.campus360.studentlife.domain.*;
import com.campus360.studentlife.repository.*;
import com.campus360.studentlife.web.dto.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Student life services: grievances/support tickets, leave requests,
 * and document requests — with approval workflows.
 */
@Service
@Transactional
public class StudentLifeService {

    private final GrievanceRepository grievances;
    private final LeaveRequestRepository leaves;
    private final DocumentRequestRepository documents;
    private final ApplicationEventPublisher events;

    public StudentLifeService(GrievanceRepository grievances, LeaveRequestRepository leaves,
                              DocumentRequestRepository documents, ApplicationEventPublisher events) {
        this.grievances = grievances;
        this.leaves = leaves;
        this.documents = documents;
        this.events = events;
    }

    // ---- Grievances ----
    public Grievance createGrievance(GrievanceRequest req) {
        Long tenant = TenantContext.requireTenantId();
        Grievance g = new Grievance();
        g.setTenantId(tenant);
        g.setUserId(CurrentUser.id());
        g.setCategory(req.category() != null ? req.category().toUpperCase() : "GENERAL");
        g.setSubject(req.subject());
        g.setDescription(req.description());
        if (req.priority() != null) g.setPriority(req.priority().toUpperCase());
        return grievances.save(g);
    }

    @Transactional(readOnly = true)
    public PageResponse<Grievance> listGrievances(int page, int size, String status) {
        Long tenant = TenantContext.requireTenantId();
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        if (status != null && !status.isBlank()) {
            return PageResponse.of(grievances.findByTenantIdAndStatusOrderByCreatedAtDesc(tenant, status.toUpperCase(), pageable));
        }
        return PageResponse.of(grievances.findByTenantIdOrderByCreatedAtDesc(tenant, pageable));
    }

    @Transactional(readOnly = true)
    public List<Grievance> myGrievances() {
        return grievances.findByTenantIdAndUserId(TenantContext.requireTenantId(), CurrentUser.id());
    }

    public Grievance updateGrievanceStatus(Long id, String status, String resolution) {
        Grievance g = grievances.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Grievance not found"));
        g.setStatus(status.toUpperCase());
        if (resolution != null) g.setResolution(resolution);
        g.setUpdatedAt(Instant.now());
        return grievances.save(g);
    }

    public Grievance assignGrievance(Long id, Long assigneeId) {
        Grievance g = grievances.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Grievance not found"));
        g.setAssignedTo(assigneeId);
        g.setStatus("IN_PROGRESS");
        g.setUpdatedAt(Instant.now());
        return grievances.save(g);
    }

    // ---- Leave requests ----
    public LeaveRequest createLeaveRequest(LeaveRequestDto req) {
        Long tenant = TenantContext.requireTenantId();
        LeaveRequest lr = new LeaveRequest();
        lr.setTenantId(tenant);
        lr.setStudentId(req.studentId());
        lr.setSectionId(req.sectionId());
        lr.setLeaveType(req.leaveType() != null ? req.leaveType().toUpperCase() : "PERSONAL");
        lr.setStartDate(req.startDate());
        lr.setEndDate(req.endDate());
        lr.setReason(req.reason());
        return leaves.save(lr);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> studentLeaves(Long studentId) {
        return leaves.findByTenantIdAndStudentId(TenantContext.requireTenantId(), studentId);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> pendingLeaves() {
        return leaves.findByTenantIdAndStatus(TenantContext.requireTenantId(), "PENDING");
    }

    public LeaveRequest reviewLeave(Long id, String decision) {
        LeaveRequest lr = leaves.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Leave request not found"));
        String d = decision.toUpperCase();
        if (!d.equals("APPROVED") && !d.equals("REJECTED")) {
            throw ApiException.badRequest("Decision must be APPROVED or REJECTED");
        }
        lr.setStatus(d);
        lr.setReviewedBy(CurrentUser.principal() != null ? CurrentUser.principal().email() : "system");
        lr.setReviewedAt(Instant.now());
        return leaves.save(lr);
    }

    // ---- Document requests ----
    public DocumentRequest createDocumentRequest(DocumentRequestDto req) {
        Long tenant = TenantContext.requireTenantId();
        DocumentRequest dr = new DocumentRequest();
        dr.setTenantId(tenant);
        dr.setStudentId(req.studentId());
        dr.setDocType(req.docType().toUpperCase());
        dr.setPurpose(req.purpose());
        if (req.copies() > 0) dr.setCopies(req.copies());
        return documents.save(dr);
    }

    @Transactional(readOnly = true)
    public List<DocumentRequest> studentDocuments(Long studentId) {
        return documents.findByTenantIdAndStudentId(TenantContext.requireTenantId(), studentId);
    }

    @Transactional(readOnly = true)
    public List<DocumentRequest> pendingDocuments() {
        return documents.findByTenantIdAndStatus(TenantContext.requireTenantId(), "PENDING");
    }

    public DocumentRequest updateDocumentStatus(Long id, String status) {
        DocumentRequest dr = documents.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Document request not found"));
        dr.setStatus(status.toUpperCase());
        dr.setReviewedBy(CurrentUser.principal() != null ? CurrentUser.principal().email() : "system");
        dr.setUpdatedAt(Instant.now());

        if ("READY".equals(status.toUpperCase())) {
            events.publishEvent(NotificationEvent.of(dr.getTenantId(), "DOCUMENT_READY",
                    "Document ready for collection",
                    "Your " + dr.getDocType() + " document is ready for pickup."));
        }
        return documents.save(dr);
    }
}
