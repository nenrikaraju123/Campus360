package com.campus360.exams.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "exam_components")
@Getter
@Setter
public class ExamComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "exam_cycle_id", nullable = false)
    private Long examCycleId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    /** e.g., MIDTERM, FINAL, PRACTICAL, INTERNAL */
    @Column(name = "component_name", nullable = false, length = 50)
    private String componentName;

    @Column(name = "max_marks", nullable = false)
    private BigDecimal maxMarks;

    @Column(name = "passing_marks")
    private BigDecimal passingMarks;

    @Column(name = "weightage_pct")
    private BigDecimal weightagePct = new BigDecimal("100.00");

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
