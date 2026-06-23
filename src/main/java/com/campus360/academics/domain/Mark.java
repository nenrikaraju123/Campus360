package com.campus360.academics.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/** A score recorded for a student on a specific assessment. */
@Entity
@Table(name = "marks")
@Getter
@Setter
public class Mark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "assessment_id", nullable = false)
    private Long assessmentId;

    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal score;

    @Column(length = 500)
    private String remarks;

    @Column(name = "graded_by", length = 120)
    private String gradedBy;

    @Column(name = "graded_at")
    private Instant gradedAt = Instant.now();
}
