package com.campus360.faculty.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Many-to-many: a faculty member may belong to multiple departments. */
@Entity
@Table(name = "faculty_department_assignments")
@Getter
@Setter
public class FacultyDepartmentAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "faculty_id", nullable = false)
    private Long facultyId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "assigned_at", updatable = false)
    private Instant assignedAt = Instant.now();
}
