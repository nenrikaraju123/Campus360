package com.campus360.finance.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_allocations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_line_id", nullable = false)
    private InvoiceLineItem invoiceLineItem;

    @Column(name = "allocated_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal allocatedAmount;

    @Column(name = "allocated_at", nullable = false, updatable = false)
    private Instant allocatedAt = Instant.now();

    @Column(name = "allocated_by", length = 120)
    private String allocatedBy;
}
