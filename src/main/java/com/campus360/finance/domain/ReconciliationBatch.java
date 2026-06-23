package com.campus360.finance.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "reconciliation_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "bank_account_id")
    private Long bankAccountId;

    @Column(name = "batch_date", nullable = false)
    private LocalDate batchDate;

    @Column(name = "total_records")
    private Integer totalRecords = 0;

    @Column(name = "matched_records")
    private Integer matchedRecords = 0;

    @Column(name = "status", length = 30)
    private String status = "PENDING"; // PENDING, COMPLETED, DISCREPANCY

    @Column(name = "uploaded_by", length = 120)
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;
}
