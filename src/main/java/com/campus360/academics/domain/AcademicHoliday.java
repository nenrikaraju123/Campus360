package com.campus360.academics.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/** Distinct holidays affecting the institution schedule. */
@Entity
@Table(name = "academic_holidays")
@Getter
@Setter
public class AcademicHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(nullable = false, length = 100)
    private String name;

    /** GENERAL, OPTIONAL, REGIONAL */
    @Column(name = "holiday_type", length = 30)
    private String holidayType = "GENERAL";

    @Column(name = "is_optional")
    private Boolean isOptional = false;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
