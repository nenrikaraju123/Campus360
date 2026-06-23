package com.campus360.finance.repository;

import com.campus360.finance.domain.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {
    List<FeeStructure> findByTenantId(Long tenantId);
    List<FeeStructure> findByTenantIdAndProgramId(Long tenantId, Long programId);
    Optional<FeeStructure> findByIdAndTenantId(Long id, Long tenantId);
}
