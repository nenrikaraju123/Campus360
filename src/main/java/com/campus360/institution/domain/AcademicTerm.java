package com.campus360.institution.domain;

import com.campus360.platform.persistence.AuditedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "academic_terms")
@Getter
@Setter
public class AcademicTerm extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "add_drop_end")
    private LocalDate addDropEnd;

    /** PLANNED | ACTIVE | CLOSED */
    @Column(nullable = false, length = 20)
    private String status = "PLANNED";
}
