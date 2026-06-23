package com.campus360.faculty.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Links a faculty member to a specific course taught in a section for a term. */
@Entity
@Table(name = "faculty_course_assignments")
@Getter
@Setter
public class FacultyCourseAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "faculty_id", nullable = false)
    private Long facultyId;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "term_id")
    private Long termId;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "assigned_at", updatable = false)
    private Instant assignedAt = Instant.now();

    /** ACTIVE, COMPLETED, CANCELLED */
    @Column(length = 20)
    private String status = "ACTIVE";
}
