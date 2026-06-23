package com.campus360.timetable.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "timetable_conflicts")
@Getter
@Setter
public class TimetableConflict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    /** FACULTY_DOUBLE_BOOKED, ROOM_DOUBLE_BOOKED, SECTION_DOUBLE_BOOKED */
    @Column(name = "conflict_type", nullable = false, length = 40)
    private String conflictType;

    @Column(name = "entry_a_id", nullable = false)
    private Long entryAId;

    @Column(name = "entry_b_id", nullable = false)
    private Long entryBId;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean resolved = false;

    @Column(name = "detected_at", updatable = false)
    private Instant detectedAt = Instant.now();
}
