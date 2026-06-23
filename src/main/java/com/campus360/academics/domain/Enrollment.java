package com.campus360.academics.domain;

import com.campus360.platform.persistence.AuditedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * A student's registration in a specific section of a course for a term.
 * Tracks enrollment status and final grade once assigned.
 */
@Entity
@Table(name = "enrollments")
@Getter
@Setter
public class Enrollment extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(name = "term_id", nullable = false)
    private Long termId;

    /** ENROLLED, DROPPED, WAITLISTED, COMPLETED */
    @Column(nullable = false, length = 20)
    private String status = "ENROLLED";

    /** Letter grade (A+, A, B+, B, C, D, F, W, I) — set when grading is finalized. */
    @Column(length = 5)
    private String grade;

    @Column(name = "grade_points", precision = 4, scale = 2)
    private java.math.BigDecimal gradePoints;

    @Version
    private int version;
}
