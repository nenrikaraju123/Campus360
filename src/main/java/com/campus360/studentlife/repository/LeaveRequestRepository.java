package com.campus360.studentlife.repository;

import com.campus360.studentlife.domain.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Optional<LeaveRequest> findByIdAndTenantId(Long id, Long tenantId);
    List<LeaveRequest> findByTenantIdAndStudentId(Long tenantId, Long studentId);
    List<LeaveRequest> findByTenantIdAndStatus(Long tenantId, String status);
}
