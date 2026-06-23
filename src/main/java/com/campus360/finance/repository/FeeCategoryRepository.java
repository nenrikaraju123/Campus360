package com.campus360.finance.repository;

import com.campus360.finance.domain.FeeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeeCategoryRepository extends JpaRepository<FeeCategory, Long> {
    Optional<FeeCategory> findByTenantIdAndCode(Long tenantId, String code);
    List<FeeCategory> findByTenantId(Long tenantId);
}
