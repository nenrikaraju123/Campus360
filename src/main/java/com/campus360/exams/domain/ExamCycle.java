package com.campus360.exams.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "exam_cycles")
@Getter
@Setter
public class ExamCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "term_id")
    private Long termId;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /** DRAFT, SCHEDULED, ONGOING, COMPLETED, RESULTS_DECLARED */
    @Column(length = 30)
    private String status = "DRAFT";

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();
}
