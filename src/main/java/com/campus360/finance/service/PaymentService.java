package com.campus360.finance.service;

import com.campus360.finance.domain.Payment;
import com.campus360.finance.domain.PaymentAllocation;
import com.campus360.finance.domain.Invoice;
import com.campus360.finance.domain.InvoiceLineItem;
import com.campus360.finance.domain.FinanceStatusHistory;
import com.campus360.finance.repository.PaymentRepository;
import com.campus360.finance.repository.PaymentAllocationRepository;
import com.campus360.finance.repository.InvoiceRepository;
import com.campus360.finance.repository.InvoiceLineItemRepository;
import com.campus360.finance.repository.FinanceStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository lineItemRepository;
    private final FinanceStatusHistoryRepository historyRepository;

    @Transactional
    public Payment recordPayment(Payment payment, String actorId) {
        Invoice invoice = invoiceRepository.findByIdAndTenantId(payment.getInvoiceId(), payment.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        if (!"PENDING".equals(invoice.getStatus()) && !"PARTIALLY_PAID".equals(invoice.getStatus())) {
            throw new IllegalStateException("Payment can only be recorded on PENDING or PARTIALLY_PAID invoices");
        }

        Payment savedPayment = paymentRepository.save(payment);

        // Simple FIFO allocation strategy across line items
        List<InvoiceLineItem> items = lineItemRepository.findByTenantIdAndInvoice_Id(payment.getTenantId(), invoice.getId());
        BigDecimal remainingToAllocate = payment.getAmount();

        for (InvoiceLineItem item : items) {
            if (remainingToAllocate.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal lineBalance = item.getBalanceAmount();
            if (lineBalance.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal allocateToThisLine = remainingToAllocate.min(lineBalance);

                item.setAllocatedAmount(item.getAllocatedAmount().add(allocateToThisLine));
                item.setBalanceAmount(item.getBalanceAmount().subtract(allocateToThisLine));
                lineItemRepository.save(item);

                PaymentAllocation allocation = new PaymentAllocation();
                allocation.setTenantId(payment.getTenantId());
                allocation.setPayment(savedPayment);
                allocation.setInvoiceLineItem(item);
                allocation.setAllocatedAmount(allocateToThisLine);
                allocation.setAllocatedBy(actorId);
                allocationRepository.save(allocation);

                remainingToAllocate = remainingToAllocate.subtract(allocateToThisLine);
            }
        }

        // Update Invoice totals
        invoice.setPaidAmount(invoice.getPaidAmount().add(payment.getAmount()));
        String oldStatus = invoice.getStatus();

        // Let's calculate total balance across items, or just compare invoice amount
        BigDecimal totalBalance = invoice.getAmount().subtract(invoice.getPaidAmount());
        if (totalBalance.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus("PAID");
        } else {
            invoice.setStatus("PARTIALLY_PAID");
        }

        invoiceRepository.save(invoice);

        recordHistory(savedPayment.getTenantId(), "PAYMENT", savedPayment.getId(), null, "SUCCESS", actorId, "Payment recorded");
        if (!oldStatus.equals(invoice.getStatus())) {
            recordHistory(invoice.getTenantId(), "INVOICE", invoice.getId(), oldStatus, invoice.getStatus(), actorId, "Payment applied changing invoice status");
        }

        return savedPayment;
    }

    @Transactional
    public void cancelPayment(Long tenantId, Long paymentId, String actorId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (!payment.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Tenant mismatch");
        }

        Invoice invoice = invoiceRepository.findByIdAndTenantId(payment.getInvoiceId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        List<PaymentAllocation> allocations = allocationRepository.findByTenantIdAndPayment_Id(tenantId, paymentId);
        
        for (PaymentAllocation allocation : allocations) {
            InvoiceLineItem item = allocation.getInvoiceLineItem();
            item.setAllocatedAmount(item.getAllocatedAmount().subtract(allocation.getAllocatedAmount()));
            item.setBalanceAmount(item.getBalanceAmount().add(allocation.getAllocatedAmount()));
            lineItemRepository.save(item);
            
            allocationRepository.delete(allocation);
        }

        invoice.setPaidAmount(invoice.getPaidAmount().subtract(payment.getAmount()));
        String oldStatus = invoice.getStatus();
        
        BigDecimal totalBalance = invoice.getAmount().subtract(invoice.getPaidAmount());
        if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus("PENDING");
        } else if (totalBalance.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus("PARTIALLY_PAID");
        }

        invoiceRepository.save(invoice);
        paymentRepository.delete(payment); // or mark as cancelled

        recordHistory(tenantId, "PAYMENT", paymentId, "SUCCESS", "CANCELLED", actorId, "Payment cancelled, allocations reversed");
        if (!oldStatus.equals(invoice.getStatus())) {
            recordHistory(tenantId, "INVOICE", invoice.getId(), oldStatus, invoice.getStatus(), actorId, "Payment cancellation changing invoice status");
        }
    }

    private void recordHistory(Long tenantId, String type, Long entityId, String oldStatus, String newStatus, String actor, String comments) {
        FinanceStatusHistory history = new FinanceStatusHistory();
        history.setTenantId(tenantId);
        history.setEntityType(type);
        history.setEntityId(entityId);
        history.setPreviousStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(actor);
        history.setComments(comments);
        historyRepository.save(history);
    }
}
