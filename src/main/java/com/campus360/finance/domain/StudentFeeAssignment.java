package com.campus360.finance.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "student_fee_assignments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "student_id", "category_id", "term_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentFeeAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private FeeCategory category;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "term_id")
    private Long termId;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt = Instant.now();

    @Column(name = "assigned_by", length = 120)
    private String assignedBy;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "ACTIVE"; // ACTIVE, CANCELLED
}
