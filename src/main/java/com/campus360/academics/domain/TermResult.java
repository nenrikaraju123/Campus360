package com.campus360.academics.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/** Snapshot of a student's SGPA/CGPA for a specific term. */
@Entity
@Table(name = "term_results")
@Getter
@Setter
public class TermResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "term_id", nullable = false)
    private Long termId;

    @Column(name = "credits_earned", nullable = false)
    private int creditsEarned;

    @Column(name = "credits_attempted", nullable = false)
    private int creditsAttempted;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal sgpa = BigDecimal.ZERO;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal cgpa = BigDecimal.ZERO;

    @Column(name = "computed_at")
    private Instant computedAt = Instant.now();
}
