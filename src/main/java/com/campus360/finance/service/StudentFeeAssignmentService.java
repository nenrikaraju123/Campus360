package com.campus360.finance.service;

import com.campus360.finance.domain.StudentFeeAssignment;
import com.campus360.finance.repository.StudentFeeAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentFeeAssignmentService {

    private final StudentFeeAssignmentRepository repository;

    @Transactional(readOnly = true)
    public List<StudentFeeAssignment> getAssignments(Long tenantId, Long studentId) {
        return repository.findByTenantIdAndStudentId(tenantId, studentId);
    }

    @Transactional
    public StudentFeeAssignment assignFee(StudentFeeAssignment assignment) {
        return repository.save(assignment);
    }
}
