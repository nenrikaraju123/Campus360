package com.campus360.finance.repository;

import com.campus360.finance.domain.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByTenantIdAndStudentId(Long tenantId, Long studentId);
}
