package com.campus360.finance.repository;

import com.campus360.finance.domain.PaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, Long> {
    List<PaymentAllocation> findByTenantIdAndPayment_Id(Long tenantId, Long paymentId);
}
