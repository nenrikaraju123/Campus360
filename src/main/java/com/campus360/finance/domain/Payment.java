package com.campus360.finance.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/** Payment record for an invoice. */
@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** ONLINE, CASH, CHEQUE, UPI, BANK_TRANSFER */
    @Column(name = "payment_method", length = 40)
    private String paymentMethod;

    @Column(name = "transaction_ref", length = 120)
    private String transactionRef;

    @Column(name = "paid_at")
    private Instant paidAt = Instant.now();

    @Column(name = "recorded_by", length = 120)
    private String recordedBy;
}
