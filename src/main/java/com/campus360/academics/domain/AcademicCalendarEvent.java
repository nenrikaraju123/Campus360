package com.campus360.academics.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/** Important events in the academic calendar (Exams, Holidays, Fee Dues). */
@Entity
@Table(name = "academic_calendar_events")
@Getter
@Setter
public class AcademicCalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    /** HOLIDAY, EXAM, FEE_DUE, PLACEMENT_DRIVE, ACADEMIC_EVENT, ADMISSION_EVENT, CUSTOM */
    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** INSTITUTION, PROGRAM, SECTION */
    @Column(length = 30)
    private String scope = "INSTITUTION";

    @Column(name = "scope_id")
    private Long scopeId;

    @Column(name = "is_all_day")
    private Boolean isAllDay = true;

    @Column(name = "created_by", length = 120)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();
}
