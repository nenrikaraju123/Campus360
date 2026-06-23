package com.campus360.institution.domain;

import com.campus360.platform.persistence.AuditedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** A course offering in a given term, taught by a faculty member. */
@Entity
@Table(name = "sections")
@Getter
@Setter
public class Section extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "term_id", nullable = false)
    private Long termId;

    @Column(name = "faculty_user_id")
    private Long facultyUserId;

    @Column(nullable = false)
    private int capacity = 60;

    @Column(length = 200)
    private String schedule;
}
