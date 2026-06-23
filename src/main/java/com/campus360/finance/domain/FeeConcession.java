package com.campus360.finance.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fee_concessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeeConcession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private FeeCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id")
    private FeeComponent component;

    @Column(name = "concession_type", nullable = false, length = 50)
    private String concessionType; // PERCENTAGE, FIXED_AMOUNT

    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "status", length = 30)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, APPLIED

    @Column(name = "approved_by", length = 120)
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
