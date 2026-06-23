package com.campus360.admissions.web;

import com.campus360.admissions.domain.AdmissionApplication;
import com.campus360.admissions.service.AdmissionWorkflowService;
import com.campus360.admissions.web.dto.WorkflowActionRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admissions/offers")
@RequiredArgsConstructor
@Tag(name = "Admissions", description = "Admission offer lifecycle management")
public class AdmissionOfferController {

    private final AdmissionWorkflowService workflowService;

    @PostMapping("/{id}/actions/accept")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN', 'PARENT', 'STUDENT')")
    public AdmissionApplication acceptOffer(@PathVariable Long id, @RequestBody(required = false) WorkflowActionRequest req) {
        // Here ID is the application ID, typically an offer is tied 1:1 with an application.
        return workflowService.transition(id, "ACCEPTED", req != null ? req.comment : "Offer accepted");
    }
}
