package com.campus360.exams.web;

import com.campus360.exams.domain.ExamMark;
import com.campus360.exams.domain.ExamMarkSheet;
import com.campus360.exams.service.ExamMarkSheetService;
import com.campus360.exams.service.ExamWorkflowService;
import com.campus360.exams.web.dto.CreateExamMarkSheetRequest;
import com.campus360.exams.web.dto.UpdateExamMarksRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams/mark-sheets")
public class ExamMarkSheetController {

    private final ExamMarkSheetService service;
    private final ExamWorkflowService workflowService;

    public ExamMarkSheetController(ExamMarkSheetService service, ExamWorkflowService workflowService) {
        this.service = service;
        this.workflowService = workflowService;
    }

    @PostMapping("/cycle/{cycleId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD', 'FACULTY')")
    public ExamMarkSheet createMarkSheet(@PathVariable Long cycleId, @Valid @RequestBody CreateExamMarkSheetRequest req) {
        return service.createMarkSheet(cycleId, req);
    }

    @PutMapping("/{id}/marks")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD', 'FACULTY')")
    public void updateMarks(@PathVariable Long id, @Valid @RequestBody UpdateExamMarksRequest req) {
        service.updateMarks(id, req);
    }

    @GetMapping("/{id}/marks")
    @PreAuthorize("isAuthenticated()")
    public List<ExamMark> getMarks(@PathVariable Long id) {
        return service.getMarks(id);
    }

    // ---- Workflow Actions ----
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'FACULTY')")
    public ExamMarkSheet submit(@PathVariable Long id) {
        return workflowService.submitMarkSheet(id);
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD', 'EXAM_CONTROLLER')")
    public ExamMarkSheet returnToDraft(@PathVariable Long id, @RequestParam String comments) {
        return workflowService.returnToDraft(id, comments);
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD', 'EXAM_CONTROLLER')")
    public ExamMarkSheet review(@PathVariable Long id) {
        return workflowService.reviewMarkSheet(id);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'EXAM_CONTROLLER')")
    public ExamMarkSheet publish(@PathVariable Long id) {
        return workflowService.publishMarkSheet(id);
    }
}
