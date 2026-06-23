package com.campus360.finance.repository;

import com.campus360.finance.domain.ReconciliationBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReconciliationBatchRepository extends JpaRepository<ReconciliationBatch, Long> {
    List<ReconciliationBatch> findByTenantIdOrderByBatchDateDesc(Long tenantId);
}
