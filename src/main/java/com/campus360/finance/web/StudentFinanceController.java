package com.campus360.finance.web;

import com.campus360.finance.domain.StudentFeeAssignment;
import com.campus360.finance.service.StudentFeeAssignmentService;
import com.campus360.platform.tenancy.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
public class StudentFinanceController {

    private final StudentFeeAssignmentService assignmentService;

    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'FINANCE', 'STUDENT', 'PARENT')")
    @GetMapping("/students/{studentId}/fee-assignments")
    public List<StudentFeeAssignment> getAssignments(@PathVariable Long studentId) {
        return assignmentService.getAssignments(TenantContext.requireTenantId(), studentId);
    }

    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'FINANCE')")
    @PostMapping("/students/{studentId}/fee-assignments")
    public StudentFeeAssignment assignFee(@PathVariable Long studentId, @Valid @RequestBody StudentFeeAssignment assignment) {
        assignment.setTenantId(TenantContext.requireTenantId());
        assignment.setStudentId(studentId);
        return assignmentService.assignFee(assignment);
    }

    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'FINANCE', 'STUDENT', 'PARENT')")
    @GetMapping("/students/{studentId}/ledger")
    public Object getLedger(@PathVariable Long studentId) {
        // Placeholder for ledger implementation
        return List.of();
    }
}
