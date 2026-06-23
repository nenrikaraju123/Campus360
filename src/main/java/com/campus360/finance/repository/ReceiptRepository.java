package com.campus360.finance.repository;

import com.campus360.finance.domain.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByTenantIdAndReceiptNumber(Long tenantId, String receiptNumber);
}
