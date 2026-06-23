package com.campus360.finance.repository;

import com.campus360.finance.domain.ReconciliationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReconciliationItemRepository extends JpaRepository<ReconciliationItem, Long> {
    List<ReconciliationItem> findByTenantIdAndBatch_Id(Long tenantId, Long batchId);
}
