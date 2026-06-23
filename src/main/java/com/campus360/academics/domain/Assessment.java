package com.campus360.academics.domain;

import com.campus360.platform.persistence.AuditedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/** Assessment definition (quiz, assignment, midterm, etc.) within a section. */
@Entity
@Table(name = "assessments")
@Getter
@Setter
public class Assessment extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(nullable = false, length = 200)
    private String title;

    /** QUIZ, ASSIGNMENT, MIDTERM, ENDTERM, LAB, PROJECT */
    @Column(nullable = false, length = 30)
    private String type = "ASSIGNMENT";

    @Column(name = "max_marks", nullable = false, precision = 6, scale = 2)
    private BigDecimal maxMarks;

    @Column(name = "weightage_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightagePct = BigDecimal.valueOf(100);

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(length = 2000)
    private String instructions;
}
