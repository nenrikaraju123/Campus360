package com.campus360.institution.repository;

import com.campus360.institution.domain.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    List<Program> findByTenantId(Long tenantId);

    List<Program> findByTenantIdAndDepartmentId(Long tenantId, Long departmentId);

    Optional<Program> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByTenantIdAndCodeIgnoreCase(Long tenantId, String code);
}
