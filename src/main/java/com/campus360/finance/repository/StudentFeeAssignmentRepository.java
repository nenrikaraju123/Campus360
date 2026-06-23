package com.campus360.finance.repository;

import com.campus360.finance.domain.StudentFeeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentFeeAssignmentRepository extends JpaRepository<StudentFeeAssignment, Long> {
    List<StudentFeeAssignment> findByTenantIdAndStudentId(Long tenantId, Long studentId);
}
