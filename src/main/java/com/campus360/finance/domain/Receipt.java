package com.campus360.finance.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "receipts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "receipt_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "receipt_number", nullable = false, length = 50)
    private String receiptNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt = Instant.now();

    @Column(name = "issued_by", length = 120)
    private String issuedBy;

    @Column(name = "status", length = 30)
    private String status = "ISSUED"; // ISSUED, CANCELLED
}
