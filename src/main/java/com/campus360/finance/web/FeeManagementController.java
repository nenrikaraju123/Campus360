package com.campus360.finance.web;

import com.campus360.finance.domain.*;
import com.campus360.finance.service.FeeService;
import com.campus360.finance.web.dto.*;
import com.campus360.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Unified controller for fee structures, invoices, payments, and student fee summaries.
 * Delegates to {@link FeeService} which handles invoice auto-status transitions.
 */
@RestController
@RequestMapping("/api/v1/finance")
@Tag(name = "Finance", description = "Fee structures, invoices, payments, summaries")
public class FeeManagementController {

    private final FeeService service;

    public FeeManagementController(FeeService service) {
        this.service = service;
    }

    // ──────────────────────────────────────────────
    // Fee Structures
    // ──────────────────────────────────────────────

    @PostMapping("/fees")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'FINANCE')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a fee structure (tuition, hostel, lab, etc.)")
    public FeeStructure createFee(@Valid @RequestBody FeeStructureRequest req) {
        return service.createFeeStructure(req);
    }

    @GetMapping("/fees")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD', 'FINANCE')")
    @Operation(summary = "List fee structures, optionally filtered by program")
    public List<FeeStructure> fees(@RequestParam(required = false) Long programId) {
        return service.listFeeStructures(programId);
    }

    // ──────────────────────────────────────────────
    // Invoices
    // ──────────────────────────────────────────────

    @PostMapping("/invoices")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'FINANCE')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generate an invoice for a student")
    public Invoice generateInvoice(@Valid @RequestBody GenerateInvoiceRequest req) {
        return service.generateInvoice(req);
    }

    @PostMapping("/invoices/bulk/{feeStructureId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'FINANCE')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Bulk-generate invoices for all students in the fee's program")
    public List<Invoice> bulkGenerate(@PathVariable Long feeStructureId) {
        return service.bulkGenerateInvoices(feeStructureId);
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD', 'FINANCE')")
    @Operation(summary = "List all invoices (paginated, optionally filtered by status)")
    public PageResponse<Invoice> invoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return service.listInvoices(page, size, status);
    }

    @GetMapping("/invoices/by-student/{studentId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD', 'STUDENT', 'PARENT')")
    @Operation(summary = "List all invoices for a specific student")
    public List<Invoice> studentInvoices(@PathVariable Long studentId) {
        return service.studentInvoices(studentId);
    }

    @PostMapping("/invoices/{id}/waive")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'FINANCE')")
    @Operation(summary = "Waive an invoice")
    public Invoice waive(@PathVariable Long id) {
        return service.waiveInvoice(id);
    }

    // ──────────────────────────────────────────────
    // Payments (via FeeService for auto-status transitions)
    // ──────────────────────────────────────────────

    @PostMapping("/payments")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD', 'FINANCE')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record a payment against an invoice (auto-updates invoice status)")
    public Payment recordPayment(@Valid @RequestBody RecordPaymentRequest req) {
        return service.recordPayment(req);
    }

    @GetMapping("/payments/by-invoice/{invoiceId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD', 'STUDENT', 'PARENT')")
    @Operation(summary = "List payments for an invoice")
    public List<Payment> invoicePayments(@PathVariable Long invoiceId) {
        return service.invoicePayments(invoiceId);
    }

    // ──────────────────────────────────────────────
    // Student Fee Summary
    // ──────────────────────────────────────────────

    @GetMapping("/summary/{studentId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD', 'STUDENT', 'PARENT')")
    @Operation(summary = "Fee summary for a student: total due, paid, outstanding, overdue")
    public FeeSummary studentSummary(@PathVariable Long studentId) {
        return service.studentFeeSummary(studentId);
    }
}
