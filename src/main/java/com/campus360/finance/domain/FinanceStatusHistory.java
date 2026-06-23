package com.campus360.finance.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "finance_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinanceStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // INVOICE, PAYMENT, REFUND, CONCESSION

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "previous_status", length = 50)
    private String previousStatus;

    @Column(name = "new_status", nullable = false, length = 50)
    private String newStatus;

    @Column(name = "changed_by", nullable = false, length = 120)
    private String changedBy;

    @Column(name = "comments", length = 500)
    private String comments;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt = Instant.now();
}
