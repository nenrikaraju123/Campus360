package com.campus360.placement.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "applications")
@Getter
@Setter
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "posting_id", nullable = false)
    private Long postingId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    /** APPLIED | SHORTLISTED | REJECTED | OFFERED | WITHDRAWN */
    @Column(nullable = false, length = 20)
    private String status = "APPLIED";

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    private int version;
}
