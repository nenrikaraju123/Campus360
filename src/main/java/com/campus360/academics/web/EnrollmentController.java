package com.campus360.academics.web;

import com.campus360.academics.domain.Enrollment;
import com.campus360.academics.service.EnrollmentService;
import com.campus360.academics.web.dto.EnrollmentRequest;
import com.campus360.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
@Tag(name = "Enrollments", description = "Course registration & enrollment management")
public class EnrollmentController {

    private final EnrollmentService service;

    public EnrollmentController(EnrollmentService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY','STUDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enroll a student in a section (auto-waitlists if capacity full)")
    public Enrollment enroll(@Valid @RequestBody EnrollmentRequest req) {
        return service.enroll(req);
    }

    @PostMapping("/{id}/drop")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY','STUDENT')")
    @Operation(summary = "Drop a student from a section")
    public Enrollment drop(@PathVariable Long id) {
        return service.drop(id);
    }

    @GetMapping("/{id}")
    public Enrollment get(@PathVariable Long id) {
        return service.getEnrollment(id);
    }

    @GetMapping("/by-term/{termId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    @Operation(summary = "List all enrollments for a term (paginated)")
    public PageResponse<Enrollment> listByTerm(
            @PathVariable Long termId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return PageResponse.of(service.listByTerm(termId,
                PageRequest.of(page, Math.min(size, 200), Sort.by("studentId"))));
    }

    @GetMapping("/by-student/{studentId}")
    @Operation(summary = "All enrollments for a student")
    public List<Enrollment> listByStudent(@PathVariable Long studentId) {
        return service.listByStudent(studentId);
    }

    @GetMapping("/by-section/{sectionId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    @Operation(summary = "All enrollments in a section")
    public List<Enrollment> listBySection(@PathVariable Long sectionId) {
        return service.listBySection(sectionId);
    }

    @GetMapping("/active/by-student/{studentId}")
    @Operation(summary = "Currently active enrollments for a student")
    public List<Enrollment> activeByStudent(@PathVariable Long studentId) {
        return service.activeByStudent(studentId);
    }
}
