package com.campus360.institution.repository;

import com.campus360.institution.domain.AcademicTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicTermRepository extends JpaRepository<AcademicTerm, Long> {

    List<AcademicTerm> findByTenantId(Long tenantId);

    Optional<AcademicTerm> findByIdAndTenantId(Long id, Long tenantId);
}
