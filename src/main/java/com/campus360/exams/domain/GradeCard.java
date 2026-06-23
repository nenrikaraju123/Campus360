package com.campus360.exams.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "grade_cards")
@Getter
@Setter
public class GradeCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "exam_cycle_id", nullable = false)
    private Long examCycleId;

    @Column(name = "term_id")
    private Long termId;

    @Column(name = "total_credits")
    private BigDecimal totalCredits;

    @Column(name = "earned_credits")
    private BigDecimal earnedCredits;

    @Column(name = "sgpa")
    private BigDecimal sgpa;

    @Column(name = "cgpa")
    private BigDecimal cgpa;

    /** PASS, FAIL, PROMOTED, WITHHELD */
    @Column(name = "result_status", length = 30)
    private String resultStatus;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "generated_at", updatable = false)
    private Instant generatedAt = Instant.now();
}
