package com.campus360.faculty.web;

import com.campus360.faculty.domain.FacultyCourseAssignment;
import com.campus360.faculty.domain.FacultyProfile;
import com.campus360.faculty.service.FacultyService;
import com.campus360.faculty.web.dto.BulkCreateFacultyResult;
import com.campus360.faculty.web.dto.BulkFacultyRequest;
import com.campus360.faculty.web.dto.CreateFacultyRequest;
import com.campus360.faculty.web.dto.FacultyCourseAssignmentRequest;
import com.campus360.platform.security.CurrentUser;
import com.campus360.shared.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/faculty")
public class FacultyController {

    private final FacultyService service;

    public FacultyController(FacultyService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public FacultyProfile create(@Valid @RequestBody CreateFacultyRequest req) {
        return service.create(req);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public List<BulkCreateFacultyResult> bulkCreate(@Valid @RequestBody List<BulkFacultyRequest> requests) {
        return service.bulkCreateFaculty(requests);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public PageResponse<FacultyProfile> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD', 'FACULTY')")
    public FacultyProfile get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public FacultyProfile update(@PathVariable Long id, @Valid @RequestBody CreateFacultyRequest req) {
        return service.update(id, req);
    }

    @PostMapping("/{id}/course-assignments")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public FacultyCourseAssignment assignCourse(@PathVariable Long id,
                                                 @Valid @RequestBody FacultyCourseAssignmentRequest req) {
        return service.assignCourse(id, req);
    }

    @GetMapping("/me/courses")
    @PreAuthorize("hasRole('FACULTY')")
    public List<FacultyCourseAssignment> myCourses() {
        Long userId = CurrentUser.principal().userId();
        FacultyProfile profile = service.getByUserId(userId);
        return service.listCourseAssignments(profile.getId());
    }

    @PostMapping("/bulk-import/template")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public void generateImportTemplate() {
        // Logic to generate template
    }

    @PostMapping("/bulk-import")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public Object uploadBulkImport() {
        // Logic to upload file and create ImportJob
        return null;
    }

    @GetMapping("/bulk-import/{jobId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public Object getBulkImportJob(@PathVariable Long jobId) {
        // Return job details
        return null;
    }

    @PostMapping("/bulk-import/{jobId}/actions/commit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public void commitBulkImport(@PathVariable Long jobId) {
        // Trigger async commit
    }
}
