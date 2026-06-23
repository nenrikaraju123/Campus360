package com.campus360.admissions.web;

import com.campus360.admissions.domain.AdmissionApplication;
import com.campus360.admissions.domain.AdmissionNote;
import com.campus360.admissions.domain.AdmissionStatusHistory;
import com.campus360.admissions.service.AdmissionApplicationService;
import com.campus360.admissions.service.AdmissionConversionService;
import com.campus360.admissions.service.AdmissionWorkflowService;
import com.campus360.admissions.web.dto.AdmissionApplicationRequest;
import com.campus360.admissions.web.dto.WorkflowActionRequest;
import com.campus360.student.domain.StudentProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admissions/applications")
@Tag(name = "Admissions", description = "Admission application lifecycle management")
public class AdmissionApplicationController {

    private final AdmissionApplicationService applicationService;
    private final AdmissionWorkflowService workflowService;
    private final AdmissionConversionService conversionService;

    public AdmissionApplicationController(AdmissionApplicationService applicationService,
                                          AdmissionWorkflowService workflowService,
                                          AdmissionConversionService conversionService) {
        this.applicationService = applicationService;
        this.workflowService = workflowService;
        this.conversionService = conversionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN', 'HOD', 'FACULTY')")
    @Operation(summary = "List admission applications with optional status filter")
    public Page<AdmissionApplication> list(@RequestParam(required = false) String status, Pageable pageable) {
        return applicationService.list(status, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN', 'HOD', 'FACULTY')")
    public AdmissionApplication get(@PathVariable Long id) {
        return applicationService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public AdmissionApplication create(@Valid @RequestBody AdmissionApplicationRequest req) {
        return applicationService.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public AdmissionApplication update(@PathVariable Long id,
                                       @Valid @RequestBody AdmissionApplicationRequest req) {
        return applicationService.update(id, req);
    }

    @GetMapping("/{id}/status-history")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN', 'HOD')")
    public List<AdmissionStatusHistory> statusHistory(@PathVariable Long id) {
        return workflowService.getStatusHistory(id);
    }

    @PostMapping("/{id}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN', 'HOD', 'FACULTY')")
    public AdmissionNote addNote(@PathVariable Long id,
                                 @RequestBody Map<String, Object> body) {
        String content = (String) body.getOrDefault("content", "");
        boolean isInternal = (Boolean) body.getOrDefault("isInternal", true);
        return applicationService.addNote(id, content, isInternal);
    }

    // ---- Workflow Actions ----

    @PostMapping("/{id}/actions/submit-review")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public AdmissionApplication submitReview(@PathVariable Long id, @RequestBody(required = false) WorkflowActionRequest req) {
        return workflowService.transition(id, "UNDER_REVIEW", req != null ? req.comment : null);
    }

    @PostMapping("/{id}/actions/shortlist")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN', 'HOD')")
    public AdmissionApplication shortlist(@PathVariable Long id, @RequestBody(required = false) WorkflowActionRequest req) {
        return workflowService.transition(id, "SHORTLISTED", req != null ? req.comment : null);
    }

    @PostMapping("/{id}/actions/approve")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public AdmissionApplication approve(@PathVariable Long id, @RequestBody(required = false) WorkflowActionRequest req) {
        return workflowService.transition(id, "APPROVED", req != null ? req.comment : null);
    }

    @PostMapping("/{id}/actions/reject")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public AdmissionApplication reject(@PathVariable Long id, @RequestBody(required = false) WorkflowActionRequest req) {
        return workflowService.transition(id, "REJECTED", req != null ? req.comment : null);
    }

    @PostMapping("/{id}/actions/waitlist")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public AdmissionApplication waitlist(@PathVariable Long id, @RequestBody(required = false) WorkflowActionRequest req) {
        return workflowService.transition(id, "WAITLISTED", req != null ? req.comment : null);
    }

    @PostMapping("/{id}/actions/create-offer")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public AdmissionApplication createOffer(@PathVariable Long id, @RequestBody(required = false) WorkflowActionRequest req) {
        return workflowService.transition(id, "OFFERED", req != null ? req.comment : null);
    }

    @PostMapping("/{id}/actions/enroll")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public StudentProfile enroll(@PathVariable Long id, @RequestBody(required = false) WorkflowActionRequest req) {
        return conversionService.enroll(id, req != null ? req.comment : null);
    }
}
