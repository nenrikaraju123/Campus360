package com.campus360.timetable.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "timetable_templates")
@Getter
@Setter
public class TimetableTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "term_id")
    private Long termId;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    /** DRAFT, PUBLISHED, ARCHIVED */
    @Column(length = 20)
    private String status = "DRAFT";

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "published_by")
    private Long publishedBy;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();
}
