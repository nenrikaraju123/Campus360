package com.campus360.finance.repository;

import com.campus360.finance.domain.FeeConcession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeConcessionRepository extends JpaRepository<FeeConcession, Long> {
    List<FeeConcession> findByTenantIdAndStudentId(Long tenantId, Long studentId);
}
