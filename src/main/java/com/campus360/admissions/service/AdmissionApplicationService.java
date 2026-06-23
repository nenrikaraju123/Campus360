package com.campus360.admissions.service;

import com.campus360.admissions.domain.AdmissionApplication;
import com.campus360.admissions.domain.AdmissionNote;
import com.campus360.admissions.repository.AdmissionApplicationRepository;
import com.campus360.admissions.repository.AdmissionNoteRepository;
import com.campus360.admissions.web.dto.AdmissionApplicationRequest;
import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.numbering.NumberingService;
import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.tenancy.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class AdmissionApplicationService {

    private final AdmissionApplicationRepository repository;
    private final AdmissionNoteRepository noteRepository;
    private final NumberingService numberingService;
    private final AuditService auditService;

    public AdmissionApplicationService(AdmissionApplicationRepository repository,
                                       AdmissionNoteRepository noteRepository,
                                       NumberingService numberingService,
                                       AuditService auditService) {
        this.repository = repository;
        this.noteRepository = noteRepository;
        this.numberingService = numberingService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<AdmissionApplication> list(String status, Pageable pageable) {
        Long tenantId = TenantContext.requireTenantId();
        if (status != null && !status.isBlank()) {
            return repository.findByTenantIdAndStatus(tenantId, status, pageable);
        }
        return repository.findByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public AdmissionApplication get(Long id) {
        Long tenantId = TenantContext.requireTenantId();
        return repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> ApiException.notFound("Application not found: " + id));
    }

    public AdmissionApplication create(AdmissionApplicationRequest req) {
        Long tenantId = TenantContext.requireTenantId();

        // Duplicate email check per tenant
        if (repository.existsByTenantIdAndEmail(tenantId, req.email)) {
            throw ApiException.conflict("An application with email '" + req.email + "' already exists for this institution.");
        }

        AdmissionApplication app = new AdmissionApplication();
        app.setTenantId(tenantId);
        app.setLeadId(req.leadId);
        app.setFirstName(req.firstName);
        app.setLastName(req.lastName);
        app.setEmail(req.email);
        app.setPhone(req.phone);
        app.setDateOfBirth(req.dateOfBirth);
        app.setGender(req.gender);
        app.setCategory(req.category);
        app.setQuota(req.quota);
        app.setProgramId(req.programId);
        app.setDepartmentId(req.departmentId);
        app.setPreferredSectionId(req.preferredSectionId);
        app.setAcademicYear(req.academicYear);
        app.setGuardianName(req.guardianName);
        app.setGuardianEmail(req.guardianEmail);
        app.setGuardianPhone(req.guardianPhone);
        app.setCreatedBy(CurrentUser.principal() != null ? CurrentUser.principal().userId() : null);

        // Assign application number from NumberingService
        String appNumber = numberingService.generateNextNumber("ADMISSION", req.academicYear);
        app.setApplicationNumber(appNumber);

        AdmissionApplication saved = repository.save(app);
        auditService.log("ADMISSION_APPLICATION_CREATED", "AdmissionApplication", saved.getId(),
                "Email: " + req.email);
        return saved;
    }

    public AdmissionApplication update(Long id, AdmissionApplicationRequest req) {
        Long tenantId = TenantContext.requireTenantId();
        AdmissionApplication app = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> ApiException.notFound("Application not found: " + id));

        // Only mutable before review
        java.util.Set<String> terminalStatuses = java.util.Set.of("ENROLLED", "CANCELLED", "REJECTED");
        if (terminalStatuses.contains(app.getStatus())) {
            throw ApiException.badRequest("Cannot modify application in status: " + app.getStatus());
        }

        app.setFirstName(req.firstName);
        app.setLastName(req.lastName);
        app.setPhone(req.phone);
        app.setDateOfBirth(req.dateOfBirth);
        app.setGender(req.gender);
        app.setCategory(req.category);
        app.setQuota(req.quota);
        app.setProgramId(req.programId);
        app.setDepartmentId(req.departmentId);
        app.setPreferredSectionId(req.preferredSectionId);
        app.setGuardianName(req.guardianName);
        app.setGuardianEmail(req.guardianEmail);
        app.setGuardianPhone(req.guardianPhone);
        app.setUpdatedAt(Instant.now());

        return repository.save(app);
    }

    public AdmissionNote addNote(Long applicationId, String content, boolean isInternal) {
        Long tenantId = TenantContext.requireTenantId();
        repository.findByIdAndTenantId(applicationId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Application not found: " + applicationId));

        AdmissionNote note = new AdmissionNote();
        note.setTenantId(tenantId);
        note.setApplicationId(applicationId);
        note.setContent(content);
        note.setInternal(isInternal);
        note.setCreatedBy(CurrentUser.principal() != null ? CurrentUser.principal().userId() : null);
        return noteRepository.save(note);
    }
}
