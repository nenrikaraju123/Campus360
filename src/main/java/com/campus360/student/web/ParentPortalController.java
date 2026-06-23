package com.campus360.student.web;

import com.campus360.platform.error.ApiException;
import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.security.JwtAuthFilter.AuthPrincipal;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.student.domain.ParentStudentLink;
import com.campus360.student.domain.StudentProfile;
import com.campus360.student.repository.ParentStudentLinkRepository;
import com.campus360.student.repository.StudentProfileRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Parent portal: strictly scoped to students linked via parent_student_links.
 * A parent CANNOT access any student by guessing an ID.
 */
@RestController
@RequestMapping("/api/v1/parents/me")
@PreAuthorize("hasRole('PARENT')")
@Tag(name = "Parent Portal", description = "Parent access to linked student data")
public class ParentPortalController {

    private final ParentStudentLinkRepository linkRepository;
    private final StudentProfileRepository studentRepository;

    public ParentPortalController(ParentStudentLinkRepository linkRepository,
                                  StudentProfileRepository studentRepository) {
        this.linkRepository = linkRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/students")
    @Operation(summary = "List students linked to the authenticated parent")
    public List<StudentProfile> myStudents() {
        Long parentId = requireParentId();
        List<ParentStudentLink> links = linkRepository.findByParentIdAndIsActiveTrue(parentId);
        return links.stream()
                .map(link -> studentRepository.findByIdAndTenantId(link.getStudentId(), TenantContext.requireTenantId())
                        .orElse(null))
                .filter(s -> s != null)
                .toList();
    }

    @GetMapping("/students/{studentId}/overview")
    @Operation(summary = "Get overview for a linked student")
    public StudentProfile studentOverview(@PathVariable Long studentId) {
        assertLinked(studentId);
        Long tenantId = TenantContext.requireTenantId();
        return studentRepository.findByIdAndTenantId(studentId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Student not found"));
    }

    // ---- Cross-Module Aggregations ----

    @GetMapping("/students/{studentId}/attendance")
    @Operation(summary = "Get attendance summary for a linked student")
    public Object studentAttendance(@PathVariable Long studentId) {
        assertLinked(studentId);
        // Placeholder for attendance module integration
        return java.util.Map.of("studentId", studentId, "attendancePercentage", 95.5);
    }

    @GetMapping("/students/{studentId}/results")
    @Operation(summary = "Get exam results for a linked student")
    public Object studentResults(@PathVariable Long studentId) {
        assertLinked(studentId);
        // Placeholder for exam module integration
        return List.of();
    }

    @GetMapping("/students/{studentId}/fees")
    @Operation(summary = "Get fee ledger and invoices for a linked student")
    public Object studentFees(@PathVariable Long studentId) {
        assertLinked(studentId);
        // Placeholder for finance module integration
        return List.of();
    }

    // ---- Helper: Enforce parent-child link ----

    private Long requireParentId() {
        AuthPrincipal principal = CurrentUser.principal();
        if (principal == null) throw ApiException.unauthorized("Not authenticated");
        return principal.userId();
    }

    private void assertLinked(Long studentId) {
        Long parentId = requireParentId();
        Long tenantId = TenantContext.requireTenantId();
        boolean linked = linkRepository.existsByTenantIdAndParentIdAndStudentId(tenantId, parentId, studentId);
        if (!linked) {
            throw ApiException.forbidden("Access denied: not linked to student " + studentId);
        }
    }
}
