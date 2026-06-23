package com.campus360.student.repository;

import com.campus360.student.domain.StudentProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    List<StudentProfile> findByTenantId(Long tenantId);

    Page<StudentProfile> findByTenantId(Long tenantId, Pageable pageable);

    List<StudentProfile> findByTenantIdAndProgramId(Long tenantId, Long programId);

    Optional<StudentProfile> findByIdAndTenantId(Long id, Long tenantId);

    Optional<StudentProfile> findByUserId(Long userId);

    boolean existsByTenantIdAndRollNumberIgnoreCase(Long tenantId, String rollNumber);

    long countByTenantId(Long tenantId);
}
