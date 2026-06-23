package com.campus360.placement.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "offers")
@Getter
@Setter
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(precision = 12, scale = 2)
    private BigDecimal ctc;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    /** EXTENDED | ACCEPTED | DECLINED */
    @Column(nullable = false, length = 20)
    private String status = "EXTENDED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    private int version;
}
