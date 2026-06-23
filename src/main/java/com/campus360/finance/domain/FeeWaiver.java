package com.campus360.finance.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fee_waivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeeWaiver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_line_id", nullable = false)
    private InvoiceLineItem invoiceLineItem;

    @Column(name = "waived_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal waivedAmount;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "approved_by", nullable = false, length = 120)
    private String approvedBy;

    @Column(name = "approved_at", nullable = false, updatable = false)
    private Instant approvedAt = Instant.now();
}
