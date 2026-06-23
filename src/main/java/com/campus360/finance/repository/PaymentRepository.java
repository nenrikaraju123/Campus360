package com.campus360.finance.repository;

import com.campus360.finance.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoiceId(Long invoiceId);
    List<Payment> findByTenantId(Long tenantId);
}
