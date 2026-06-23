package com.campus360.finance.service;

import com.campus360.finance.domain.Invoice;
import com.campus360.finance.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FinanceReportService {

    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getCollectionSummary(Long tenantId) {
        // Simplified implementation: Fetch all invoices and aggregate manually
        // For production, use JPQL or Criteria API with GROUP BY
        List<Invoice> invoices = invoiceRepository.findByTenantIdAndStudentId(tenantId, null); // Using existing method as placeholder or find by tenant
        
        BigDecimal totalInvoiced = BigDecimal.ZERO;
        BigDecimal totalCollected = BigDecimal.ZERO;

        for (Invoice invoice : invoices) {
            totalInvoiced = totalInvoiced.add(invoice.getAmount());
            totalCollected = totalCollected.add(invoice.getPaidAmount());
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalInvoiced", totalInvoiced);
        summary.put("totalCollected", totalCollected);
        summary.put("outstanding", totalInvoiced.subtract(totalCollected));

        return summary;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDueAging(Long tenantId) {
        // Placeholder for Due Aging report logic
        Map<String, Object> aging = new HashMap<>();
        aging.put("0-30 Days", BigDecimal.ZERO);
        aging.put("31-60 Days", BigDecimal.ZERO);
        aging.put("61-90 Days", BigDecimal.ZERO);
        aging.put("> 90 Days", BigDecimal.ZERO);
        return aging;
    }
}
