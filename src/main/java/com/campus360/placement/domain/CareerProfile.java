package com.campus360.placement.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** A student's career profile (resume, skills, projects) for the placement engine. */
@Entity
@Table(name = "career_profiles")
@Getter
@Setter
public class CareerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "student_id", nullable = false, unique = true)
    private Long studentId;

    @Column(name = "resume_ref", length = 300)
    private String resumeRef;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String certifications;

    @Column(columnDefinition = "TEXT")
    private String projects;

    @Column(name = "readiness_score", nullable = false)
    private int readinessScore = 0;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
