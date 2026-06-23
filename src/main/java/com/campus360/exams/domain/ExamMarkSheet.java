package com.campus360.exams.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "exam_mark_sheets")
@Getter
@Setter
public class ExamMarkSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "exam_cycle_id", nullable = false)
    private Long examCycleId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(name = "faculty_id", nullable = false)
    private Long facultyId;

    /** DRAFT, SUBMITTED, REVIEWED, PUBLISHED */
    @Column(length = 30)
    private String status = "DRAFT";

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Version
    private Integer version = 0;
}
