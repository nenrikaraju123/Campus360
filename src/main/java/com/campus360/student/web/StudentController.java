package com.campus360.student.web;

import com.campus360.student.domain.StudentProfile;
import com.campus360.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "Students", description = "Student profiles and academic standing")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a single student (user account + profile + welcome email)")
    public StudentProfile create(@Valid @RequestBody CreateStudentRequest req) {
        return service.createStudent(req);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    @ResponseStatus(HttpStatus.MULTI_STATUS)
    @Operation(summary = "Bulk import students — each gets a user account, profile, and welcome email with login credentials")
    public List<BulkCreateStudentResult> bulkCreate(@Valid @RequestBody List<BulkStudentRequest> requests) {
        return service.bulkCreateStudents(requests);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY','PLACEMENT_OFFICER')")
    public List<StudentProfile> list() {
        return service.list();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentProfile me() {
        return service.myProfile();
    }

    @GetMapping("/{id}")
    public StudentProfile get(@PathVariable Long id) {
        return service.get(id);
    }

    @PatchMapping("/{id}/academics")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    public StudentProfile updateAcademics(@PathVariable Long id, @Valid @RequestBody UpdateAcademicsRequest req) {
        return service.updateAcademics(id, req);
    }
}

