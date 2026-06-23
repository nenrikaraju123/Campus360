package com.campus360.finance.service;

import com.campus360.finance.domain.Payment;
import com.campus360.finance.domain.Receipt;
import com.campus360.finance.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;

    @Transactional
    public Receipt generateReceipt(Payment payment, Long studentId, String issuerId) {
        Receipt receipt = new Receipt();
        receipt.setTenantId(payment.getTenantId());
        receipt.setPayment(payment);
        receipt.setStudentId(studentId);
        receipt.setAmount(payment.getAmount());
        
        // In a real scenario, use NumberingService for robust sequence generation.
        // For now, generating a unique receipt number placeholder.
        receipt.setReceiptNumber("RCT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        receipt.setIssuedBy(issuerId);
        receipt.setStatus("ISSUED");

        return receiptRepository.save(receipt);
    }
}
