package com.campus360.institution.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Maps a course to a program and a specific term/semester in the curriculum plan. */
@Entity
@Table(name = "curriculum_items")
@Getter
@Setter
public class CurriculumItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "program_id", nullable = false)
    private Long programId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "term_number", nullable = false)
    private int termNumber;

    @Column(nullable = false)
    private boolean mandatory = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
