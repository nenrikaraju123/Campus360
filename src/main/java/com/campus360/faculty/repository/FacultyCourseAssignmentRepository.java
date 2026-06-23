package com.campus360.faculty.repository;

import com.campus360.faculty.domain.FacultyCourseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacultyCourseAssignmentRepository extends JpaRepository<FacultyCourseAssignment, Long> {

    List<FacultyCourseAssignment> findByTenantIdAndFacultyId(Long tenantId, Long facultyId);

    List<FacultyCourseAssignment> findByTenantIdAndFacultyIdAndStatus(Long tenantId, Long facultyId, String status);

    List<FacultyCourseAssignment> findByTenantIdAndSectionId(Long tenantId, Long sectionId);

    Optional<FacultyCourseAssignment> findByTenantIdAndFacultyIdAndSectionIdAndCourseId(
            Long tenantId, Long facultyId, Long sectionId, Long courseId);

    boolean existsByTenantIdAndFacultyIdAndSectionId(Long tenantId, Long facultyId, Long sectionId);
}
