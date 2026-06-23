package com.campus360.finance.repository;

import com.campus360.finance.domain.FinanceStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinanceStatusHistoryRepository extends JpaRepository<FinanceStatusHistory, Long> {
    List<FinanceStatusHistory> findByTenantIdAndEntityTypeAndEntityIdOrderByChangedAtDesc(Long tenantId, String entityType, Long entityId);
}
