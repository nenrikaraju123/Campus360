package com.campus360.academics.web;

import com.campus360.academics.domain.*;
import com.campus360.academics.service.GradeService;
import com.campus360.academics.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/grades")
@Tag(name = "Grades & GPA", description = "Assessments, marks, grade finalization, GPA/CGPA computation")
public class GradeController {

    private final GradeService service;

    public GradeController(GradeService service) {
        this.service = service;
    }

    // ---- Assessments ----
    @PostMapping("/assessments")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an assessment (quiz, assignment, midterm, etc.)")
    public Assessment createAssessment(@Valid @RequestBody AssessmentRequest req) {
        return service.createAssessment(req);
    }

    @GetMapping("/assessments/by-section/{sectionId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY','STUDENT')")
    public List<Assessment> assessments(@PathVariable Long sectionId) {
        return service.listAssessments(sectionId);
    }

    @GetMapping("/assessments/{id}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY','STUDENT')")
    public Assessment assessment(@PathVariable Long id) {
        return service.getAssessment(id);
    }

    // ---- Marks ----
    @PostMapping("/marks")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enter marks in bulk for an assessment (upserts)")
    public List<Mark> enterMarks(@Valid @RequestBody BulkMarkRequest req) {
        return service.enterMarks(req);
    }

    @GetMapping("/marks/by-assessment/{assessmentId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    public List<Mark> marksForAssessment(@PathVariable Long assessmentId) {
        return service.marksForAssessment(assessmentId);
    }

    // ---- Grade finalization ----
    @PostMapping("/finalize/{sectionId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    @Operation(summary = "Compute letter grades for all enrollments in a section and trigger GPA recomputation")
    public List<Enrollment> finalizeGrades(@PathVariable Long sectionId) {
        return service.finalizeGrades(sectionId);
    }

    // ---- GPA / Transcript ----
    @PostMapping("/compute-gpa")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    @Operation(summary = "Manually recompute SGPA/CGPA for a student in a term")
    public TermResult computeGpa(@RequestParam Long studentId, @RequestParam Long termId) {
        return service.computeGPA(studentId, termId);
    }

    @GetMapping("/transcript/{studentId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY','STUDENT')")
    @Operation(summary = "Full transcript: all term results for a student")
    public List<TermResult> transcript(@PathVariable Long studentId) {
        return service.transcript(studentId);
    }
}
