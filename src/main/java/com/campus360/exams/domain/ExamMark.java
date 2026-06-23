package com.campus360.exams.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "exam_marks")
@Getter
@Setter
public class ExamMark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "mark_sheet_id", nullable = false)
    private Long markSheetId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "exam_component_id", nullable = false)
    private Long examComponentId;

    @Column(name = "marks_obtained")
    private BigDecimal marksObtained;

    @Column(name = "is_absent")
    private Boolean isAbsent = false;

    @Column(length = 200)
    private String remarks;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();
}
