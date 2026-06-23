package com.campus360.finance.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fee_components", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeeComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private FeeCategory category;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = true;

    @Column(name = "is_refundable", nullable = false)
    private Boolean isRefundable = false;

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    private BigDecimal taxPercentage = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "created_by", length = 120)
    private String createdBy;

    @Column(name = "updated_by", length = 120)
    private String updatedBy;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
