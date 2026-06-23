package com.campus360.institution.repository;

import com.campus360.institution.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByTenantId(Long tenantId);

    List<Course> findByTenantIdAndDepartmentId(Long tenantId, Long departmentId);

    Optional<Course> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByTenantIdAndCodeIgnoreCase(Long tenantId, String code);
}
