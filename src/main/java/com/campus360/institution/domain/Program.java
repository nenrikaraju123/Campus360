package com.campus360.institution.domain;

import com.campus360.platform.persistence.AuditedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "programs")
@Getter
@Setter
public class Program extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 40)
    private String level = "UNDERGRADUATE";

    @Column(name = "duration_terms", nullable = false)
    private int durationTerms = 8;

    @Column(name = "total_credits", nullable = false)
    private int totalCredits = 160;
}
