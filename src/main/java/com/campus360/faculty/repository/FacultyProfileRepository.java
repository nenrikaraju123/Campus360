package com.campus360.faculty.repository;

import com.campus360.faculty.domain.FacultyProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacultyProfileRepository extends JpaRepository<FacultyProfile, Long> {

    Optional<FacultyProfile> findByIdAndTenantId(Long id, Long tenantId);

    Optional<FacultyProfile> findByTenantIdAndUserId(Long tenantId, Long userId);

    Optional<FacultyProfile> findByTenantIdAndEmail(Long tenantId, String email);

    Optional<FacultyProfile> findByTenantIdAndEmployeeCode(Long tenantId, String employeeCode);

    Page<FacultyProfile> findByTenantId(Long tenantId, Pageable pageable);

    List<FacultyProfile> findByTenantIdAndDepartmentId(Long tenantId, Long departmentId);

    List<FacultyProfile> findByTenantIdAndStatus(Long tenantId, String status);
}
