package com.campus360.faculty.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * First-class faculty profile linked to an IAM user.
 * Employee codes are generated via {@link com.campus360.platform.numbering.NumberingService}.
 */
@Entity
@Table(name = "faculty_profiles")
@Getter
@Setter
public class FacultyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "employee_code", nullable = false, length = 40)
    private String employeeCode;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(length = 100)
    private String designation;

    @Column(length = 200)
    private String qualification;

    /** FULL_TIME, PART_TIME, VISITING, CONTRACT */
    @Column(name = "employment_type", length = 30)
    private String employmentType = "FULL_TIME";

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    /** ACTIVE, INACTIVE, ON_LEAVE, RESIGNED, TERMINATED */
    @Column(length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Version
    private Integer version = 0;
}
