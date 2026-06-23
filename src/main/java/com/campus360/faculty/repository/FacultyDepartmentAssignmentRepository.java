package com.campus360.faculty.repository;

import com.campus360.faculty.domain.FacultyDepartmentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacultyDepartmentAssignmentRepository extends JpaRepository<FacultyDepartmentAssignment, Long> {

    List<FacultyDepartmentAssignment> findByTenantIdAndFacultyId(Long tenantId, Long facultyId);

    List<FacultyDepartmentAssignment> findByTenantIdAndDepartmentId(Long tenantId, Long departmentId);
}
