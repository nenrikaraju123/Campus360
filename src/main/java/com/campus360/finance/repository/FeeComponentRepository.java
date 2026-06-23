package com.campus360.finance.repository;

import com.campus360.finance.domain.FeeComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeeComponentRepository extends JpaRepository<FeeComponent, Long> {
    List<FeeComponent> findByCategory_IdAndTenantId(Long categoryId, Long tenantId);
    Optional<FeeComponent> findByTenantIdAndCode(Long tenantId, String code);
}
