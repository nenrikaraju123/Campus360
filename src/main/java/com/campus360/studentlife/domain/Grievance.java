package com.campus360.studentlife.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

/** Grievance / support ticket raised by any user. */
@Entity
@Table(name = "grievances")
@Getter
@Setter
public class Grievance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** ACADEMIC, PLACEMENT, FEE, HOSTEL, GENERAL */
    @Column(nullable = false, length = 40)
    private String category = "GENERAL";

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, length = 4000)
    private String description;

    /** OPEN, IN_PROGRESS, RESOLVED, CLOSED */
    @Column(nullable = false, length = 20)
    private String status = "OPEN";

    /** LOW, MEDIUM, HIGH, CRITICAL */
    @Column(nullable = false, length = 10)
    private String priority = "MEDIUM";

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(length = 2000)
    private String resolution;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();
}
