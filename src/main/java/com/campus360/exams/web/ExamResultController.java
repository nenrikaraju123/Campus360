package com.campus360.exams.web;

import com.campus360.exams.domain.ResultPublication;
import com.campus360.exams.service.ExamResultService;
import com.campus360.exams.web.dto.ResultPublicationRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/exams")
public class ExamResultController {

    private final ExamResultService service;

    public ExamResultController(ExamResultService service) {
        this.service = service;
    }

    @PostMapping("/mark-sheets/{markSheetId}/generate-grade-cards")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'EXAM_CONTROLLER')")
    public void generateGradeCards(@PathVariable Long markSheetId) {
        service.generateGradeCardsForMarkSheet(markSheetId);
    }

    @PostMapping("/cycles/{cycleId}/publish-results")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'EXAM_CONTROLLER')")
    public ResultPublication publishResults(@PathVariable Long cycleId, @Valid @RequestBody ResultPublicationRequest req) {
        return service.publishResults(cycleId, req);
    }
}
