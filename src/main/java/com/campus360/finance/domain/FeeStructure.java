package com.campus360.finance.domain;

import com.campus360.platform.persistence.AuditedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A fee structure defines what a student owes (tuition, hostel, lab, etc.). */
@Entity
@Table(name = "fee_structures")
@Getter
@Setter
public class FeeStructure extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "program_id")
    private Long programId;

    @Column(name = "term_id")
    private Long termId;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** TUITION, HOSTEL, LAB, EXAM, MISC */
    @Column(name = "fee_type", nullable = false, length = 40)
    private String feeType = "TUITION";

    @Column(name = "due_date")
    private LocalDate dueDate;
}
