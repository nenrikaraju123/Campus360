package com.campus360.student.domain;

import com.campus360.platform.persistence.AuditedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
public class StudentProfile extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "program_id")
    private Long programId;

    @Column(name = "roll_number", nullable = false, length = 40)
    private String rollNumber;

    @Column(length = 80)
    private String branch;

    @Column(name = "batch_year")
    private Integer batchYear;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "current_term", nullable = false)
    private int currentTerm = 1;

    /** Cumulative GPA. Set manually now; auto-computed once the grading engine ships. */
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal cgpa = BigDecimal.ZERO;

    @Column(name = "active_backlogs", nullable = false)
    private int activeBacklogs = 0;

    // ---- Phase 2: Student 360 extensions ----

    /** Admission / application number, assigned by NumberingService. */
    @Column(name = "admission_number", length = 50)
    private String admissionNumber;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    /**
     * Student lifecycle status.
     * ACTIVE, TRANSFERRED, SUSPENDED, GRADUATED, ARCHIVED, WITHDRAWN
     */
    @Column(name = "lifecycle_status", nullable = false, length = 30)
    private String lifecycleStatus = "ACTIVE";

    /** GENERAL, OBC, SC, ST, EWS */
    @Column(length = 50)
    private String category;

    @Column(length = 50)
    private String quota;

    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String gender;

    @Column(length = 80)
    private String nationality;

    @Column(name = "emergency_contact_name", length = 120)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 30)
    private String emergencyContactPhone;

    /** GOOD_STANDING, PROBATION, SUSPENDED */
    @Column(name = "current_academic_standing", length = 30)
    private String currentAcademicStanding = "GOOD_STANDING";

    @Version
    private int version;
}

