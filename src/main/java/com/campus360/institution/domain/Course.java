package com.campus360.institution.domain;

import com.campus360.platform.persistence.AuditedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "courses")
@Getter
@Setter
public class Course extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "credit_hours", nullable = false)
    private int creditHours = 3;

    /** CORE | ELECTIVE | LAB */
    @Column(nullable = false, length = 30)
    private String type = "CORE";

    @Column(length = 1000)
    private String description;
}
