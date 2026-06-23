package com.campus360.exams.repository;

import com.campus360.exams.domain.ResultStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultStatusHistoryRepository extends JpaRepository<ResultStatusHistory, Long> {

    List<ResultStatusHistory> findByTenantIdAndEntityTypeAndEntityIdOrderByChangedAtDesc(
            Long tenantId, String entityType, Long entityId);
}
