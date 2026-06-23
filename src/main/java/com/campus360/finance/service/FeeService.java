package com.campus360.finance.service;

import com.campus360.finance.domain.*;
import com.campus360.finance.repository.*;
import com.campus360.finance.web.dto.*;
import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.shared.dto.PageResponse;
import com.campus360.student.repository.StudentProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enterprise fee management service: fee structures, invoice generation,
 * payment recording with auto-status transitions.
 */
@Service
@Transactional
public class FeeService {

    private static final AtomicLong INVOICE_SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    private final FeeStructureRepository fees;
    private final InvoiceRepository invoices;
    private final PaymentRepository payments;
    private final StudentProfileRepository students;
    private final AuditService auditService;

    public FeeService(FeeStructureRepository fees, InvoiceRepository invoices,
                      PaymentRepository payments, StudentProfileRepository students,
                      AuditService auditService) {
        this.fees = fees;
        this.invoices = invoices;
        this.payments = payments;
        this.students = students;
        this.auditService = auditService;
    }

    // ---- Fee structures ----
    public FeeStructure createFeeStructure(FeeStructureRequest req) {
        Long tenant = TenantContext.requireTenantId();
        FeeStructure f = new FeeStructure();
        f.setTenantId(tenant);
        f.setProgramId(req.programId());
        f.setTermId(req.termId());
        f.setName(req.name());
        f.setAmount(req.amount());
        if (req.feeType() != null) f.setFeeType(req.feeType().toUpperCase());
        f.setDueDate(req.dueDate());
        return fees.save(f);
    }

    @Transactional(readOnly = true)
    public List<FeeStructure> listFeeStructures(Long programId) {
        Long tenant = TenantContext.requireTenantId();
        return programId != null ? fees.findByTenantIdAndProgramId(tenant, programId)
                : fees.findByTenantId(tenant);
    }

    // ---- Invoices ----
    public Invoice generateInvoice(GenerateInvoiceRequest req) {
        Long tenant = TenantContext.requireTenantId();
        students.findByIdAndTenantId(req.studentId(), tenant)
                .orElseThrow(() -> ApiException.badRequest("Student not found: " + req.studentId()));
        FeeStructure fee = fees.findByIdAndTenantId(req.feeStructureId(), tenant)
                .orElseThrow(() -> ApiException.badRequest("Fee structure not found: " + req.feeStructureId()));

        Invoice inv = new Invoice();
        inv.setTenantId(tenant);
        inv.setStudentId(req.studentId());
        inv.setFeeStructureId(req.feeStructureId());
        inv.setAmount(fee.getAmount());
        inv.setDueDate(req.dueDate() != null ? req.dueDate() : fee.getDueDate());
        inv.setInvoiceNumber(generateInvoiceNumber(tenant));
        inv = invoices.save(inv);

        auditService.logAsync("INVOICE_GENERATED", "Invoice", inv.getId(),
                "Amount=" + inv.getAmount() + " student=" + req.studentId());
        return inv;
    }

    /** Bulk-generate invoices for all students in a program for a fee structure. */
    public List<Invoice> bulkGenerateInvoices(Long feeStructureId) {
        Long tenant = TenantContext.requireTenantId();
        FeeStructure fee = fees.findByIdAndTenantId(feeStructureId, tenant)
                .orElseThrow(() -> ApiException.badRequest("Fee structure not found"));

        var studentList = fee.getProgramId() != null
                ? students.findByTenantIdAndProgramId(tenant, fee.getProgramId())
                : students.findByTenantId(tenant);

        return studentList.stream().map(s -> {
            Invoice inv = new Invoice();
            inv.setTenantId(tenant);
            inv.setStudentId(s.getId());
            inv.setFeeStructureId(feeStructureId);
            inv.setAmount(fee.getAmount());
            inv.setDueDate(fee.getDueDate());
            inv.setInvoiceNumber(generateInvoiceNumber(tenant));
            return invoices.save(inv);
        }).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<Invoice> listInvoices(int page, int size, String status) {
        Long tenant = TenantContext.requireTenantId();
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        if (status != null && !status.isBlank()) {
            return PageResponse.of(invoices.findByTenantIdAndStatus(tenant, status.toUpperCase(), pageable));
        }
        return PageResponse.of(invoices.findByTenantId(tenant, pageable));
    }

    @Transactional(readOnly = true)
    public List<Invoice> studentInvoices(Long studentId) {
        return invoices.findByTenantIdAndStudentId(TenantContext.requireTenantId(), studentId);
    }

    public Invoice waiveInvoice(Long invoiceId) {
        Invoice inv = invoices.findByIdAndTenantId(invoiceId, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Invoice not found"));
        inv.setStatus("WAIVED");
        inv.setUpdatedAt(Instant.now());
        auditService.log("INVOICE_WAIVED", "Invoice", invoiceId, "Invoice waived");
        return invoices.save(inv);
    }

    // ---- Payments ----
    public Payment recordPayment(RecordPaymentRequest req) {
        Long tenant = TenantContext.requireTenantId();
        Invoice inv = invoices.findByIdAndTenantId(req.invoiceId(), tenant)
                .orElseThrow(() -> ApiException.notFound("Invoice not found: " + req.invoiceId()));

        if ("PAID".equals(inv.getStatus()) || "WAIVED".equals(inv.getStatus())) {
            throw ApiException.badRequest("Invoice is already " + inv.getStatus());
        }

        Payment p = new Payment();
        p.setTenantId(tenant);
        p.setInvoiceId(req.invoiceId());
        p.setAmount(req.amount());
        p.setPaymentMethod(req.paymentMethod());
        p.setTransactionRef(req.transactionRef());
        p.setRecordedBy(CurrentUser.principal() != null ? CurrentUser.principal().email() : "system");
        p = payments.save(p);

        // Update invoice paid amount and status
        BigDecimal newPaid = inv.getPaidAmount().add(req.amount());
        inv.setPaidAmount(newPaid);
        if (newPaid.compareTo(inv.getAmount()) >= 0) {
            inv.setStatus("PAID");
        } else {
            inv.setStatus("PARTIALLY_PAID");
        }
        inv.setUpdatedAt(Instant.now());
        invoices.save(inv);

        auditService.log("PAYMENT_RECORDED", "Payment", p.getId(),
                "Amount=" + req.amount() + " method=" + req.paymentMethod() + " invoice=" + req.invoiceId());
        return p;
    }

    @Transactional(readOnly = true)
    public List<Payment> invoicePayments(Long invoiceId) {
        return payments.findByInvoiceId(invoiceId);
    }

    // ---- Fee summary ----
    @Transactional(readOnly = true)
    public FeeSummary studentFeeSummary(Long studentId) {
        Long tenant = TenantContext.requireTenantId();
        List<Invoice> studentInvs = invoices.findByTenantIdAndStudentId(tenant, studentId);

        BigDecimal totalDue = studentInvs.stream()
                .filter(i -> !"WAIVED".equals(i.getStatus()))
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = studentInvs.stream()
                .map(Invoice::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pendingCount = studentInvs.stream()
                .filter(i -> "PENDING".equals(i.getStatus()) || "PARTIALLY_PAID".equals(i.getStatus()))
                .count();
        long overdueCount = studentInvs.stream()
                .filter(i -> "PENDING".equals(i.getStatus()) && i.getDueDate() != null
                        && i.getDueDate().isBefore(LocalDate.now()))
                .count();

        return new FeeSummary(studentId, totalDue, totalPaid,
                totalDue.subtract(totalPaid), pendingCount, overdueCount);
    }

    private String generateInvoiceNumber(Long tenantId) {
        return "INV-" + tenantId + "-" + INVOICE_SEQ.incrementAndGet();
    }
}
