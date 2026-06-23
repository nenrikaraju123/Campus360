package com.campus360.finance.service;

import com.campus360.finance.domain.Invoice;
import com.campus360.finance.domain.InvoiceLineItem;
import com.campus360.finance.domain.FinanceStatusHistory;
import com.campus360.finance.repository.InvoiceRepository;
import com.campus360.finance.repository.InvoiceLineItemRepository;
import com.campus360.finance.repository.FinanceStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository lineItemRepository;
    private final FinanceStatusHistoryRepository historyRepository;

    @Transactional
    public Invoice createInvoice(Invoice invoice, List<InvoiceLineItem> lineItems, String createdBy) {
        // Calculate totals server-side
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (InvoiceLineItem item : lineItems) {
            BigDecimal lineTotal = item.getAmount().add(item.getTaxAmount());
            item.setTotalAmount(lineTotal);
            item.setBalanceAmount(lineTotal);
            totalAmount = totalAmount.add(lineTotal);
        }
        invoice.setAmount(totalAmount);
        invoice.setStatus("DRAFT");
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        for (InvoiceLineItem item : lineItems) {
            item.setInvoice(savedInvoice);
            lineItemRepository.save(item);
        }

        recordHistory(savedInvoice, null, "DRAFT", createdBy, "Invoice created in DRAFT state");

        return savedInvoice;
    }

    @Transactional
    public Invoice issueInvoice(Long tenantId, Long invoiceId, String actorId) {
        Invoice invoice = invoiceRepository.findByIdAndTenantId(invoiceId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        if (!"DRAFT".equals(invoice.getStatus())) {
            throw new IllegalStateException("Only DRAFT invoices can be issued");
        }

        String oldStatus = invoice.getStatus();
        invoice.setStatus("PENDING"); // Equivalent to ISSUED/UNPAID
        Invoice saved = invoiceRepository.save(invoice);

        recordHistory(saved, oldStatus, "PENDING", actorId, "Invoice issued to student");

        return saved;
    }

    @Transactional
    public Invoice waiveInvoice(Long tenantId, Long invoiceId, String actorId, String reason) {
        Invoice invoice = invoiceRepository.findByIdAndTenantId(invoiceId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        String oldStatus = invoice.getStatus();
        invoice.setStatus("WAIVED");
        invoice.setPaidAmount(invoice.getAmount()); // Conceptually marked as fully resolved

        List<InvoiceLineItem> items = lineItemRepository.findByTenantIdAndInvoice_Id(tenantId, invoiceId);
        for (InvoiceLineItem item : items) {
            item.setWaivedAmount(item.getBalanceAmount());
            item.setBalanceAmount(BigDecimal.ZERO);
            lineItemRepository.save(item);
        }

        Invoice saved = invoiceRepository.save(invoice);
        recordHistory(saved, oldStatus, "WAIVED", actorId, "Invoice fully waived. Reason: " + reason);

        return saved;
    }

    private void recordHistory(Invoice invoice, String oldStatus, String newStatus, String actor, String comments) {
        FinanceStatusHistory history = new FinanceStatusHistory();
        history.setTenantId(invoice.getTenantId());
        history.setEntityType("INVOICE");
        history.setEntityId(invoice.getId());
        history.setPreviousStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(actor);
        history.setComments(comments);
        historyRepository.save(history);
    }
}
