package com.campus360.studentlife.repository;

import com.campus360.studentlife.domain.DocumentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DocumentRequestRepository extends JpaRepository<DocumentRequest, Long> {
    Optional<DocumentRequest> findByIdAndTenantId(Long id, Long tenantId);
    List<DocumentRequest> findByTenantIdAndStudentId(Long tenantId, Long studentId);
    List<DocumentRequest> findByTenantIdAndStatus(Long tenantId, String status);
}
