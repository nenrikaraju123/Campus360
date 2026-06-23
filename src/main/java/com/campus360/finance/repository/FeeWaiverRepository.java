package com.campus360.finance.repository;

import com.campus360.finance.domain.FeeWaiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeWaiverRepository extends JpaRepository<FeeWaiver, Long> {
    List<FeeWaiver> findByTenantIdAndInvoiceLineItem_Id(Long tenantId, Long invoiceLineItemId);
}
