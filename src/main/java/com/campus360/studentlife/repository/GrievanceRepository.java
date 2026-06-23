package com.campus360.studentlife.repository;

import com.campus360.studentlife.domain.Grievance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GrievanceRepository extends JpaRepository<Grievance, Long> {
    Optional<Grievance> findByIdAndTenantId(Long id, Long tenantId);
    Page<Grievance> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);
    Page<Grievance> findByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, String status, Pageable pageable);
    List<Grievance> findByTenantIdAndUserId(Long tenantId, Long userId);
}
