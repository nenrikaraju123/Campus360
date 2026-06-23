package com.campus360.ai.web;

import com.campus360.ai.service.CareerIntelligenceService;
import com.campus360.ai.service.JobFitReport;
import com.campus360.ai.service.ReadinessReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "Campus360 Intelligence", description = "AI-assisted career & placement guidance")
public class AiController {

    private final CareerIntelligenceService service;

    public AiController(CareerIntelligenceService service) {
        this.service = service;
    }

    @GetMapping("/students/{id}/readiness")
    @Operation(summary = "Placement readiness score + AI improvement plan")
    @PreAuthorize("hasAnyRole('STUDENT','PLACEMENT_OFFICER','INSTITUTION_ADMIN','HOD')")
    public ReadinessReport readiness(@PathVariable Long id) {
        return service.readiness(id);
    }

    @PostMapping("/students/{id}/resume-feedback")
    @PreAuthorize("hasAnyRole('STUDENT','PLACEMENT_OFFICER','INSTITUTION_ADMIN')")
    public Map<String, String> resumeFeedback(@PathVariable Long id, @RequestBody ResumeRequest req) {
        return Map.of("feedback", service.resumeFeedback(id, req.resumeText()));
    }

    @GetMapping("/students/{id}/mock-interview")
    @PreAuthorize("hasAnyRole('STUDENT','PLACEMENT_OFFICER','INSTITUTION_ADMIN')")
    public Map<String, String> mockInterview(@PathVariable Long id, @RequestParam(required = false) String role) {
        return Map.of("questions", service.mockInterview(id, role));
    }

    @GetMapping("/students/{studentId}/job-fit/{postingId}")
    @Operation(summary = "Explain student↔posting fit with eligibility gaps")
    @PreAuthorize("hasAnyRole('STUDENT','PLACEMENT_OFFICER','INSTITUTION_ADMIN')")
    public JobFitReport jobFit(@PathVariable Long studentId, @PathVariable Long postingId) {
        return service.jobFit(studentId, postingId);
    }

    record ResumeRequest(@NotBlank String resumeText) {
    }
}
