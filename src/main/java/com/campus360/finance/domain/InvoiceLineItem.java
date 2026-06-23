package com.campus360.finance.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "invoice_line_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_component_id")
    private FeeComponent feeComponent;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "allocated_amount", precision = 10, scale = 2)
    private BigDecimal allocatedAmount = BigDecimal.ZERO;

    @Column(name = "waived_amount", precision = 10, scale = 2)
    private BigDecimal waivedAmount = BigDecimal.ZERO;

    @Column(name = "balance_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
