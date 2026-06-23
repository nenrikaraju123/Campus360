package com.campus360.finance.repository;

import com.campus360.finance.domain.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByIdAndTenantId(Long id, Long tenantId);
    List<Invoice> findByTenantIdAndStudentId(Long tenantId, Long studentId);
    Page<Invoice> findByTenantId(Long tenantId, Pageable pageable);
    Page<Invoice> findByTenantIdAndStatus(Long tenantId, String status, Pageable pageable);
    List<Invoice> findByTenantIdAndStudentIdAndStatus(Long tenantId, Long studentId, String status);
}
