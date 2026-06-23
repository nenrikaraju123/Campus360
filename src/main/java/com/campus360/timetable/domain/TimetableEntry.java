package com.campus360.timetable.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "timetable_entries")
@Getter
@Setter
public class TimetableEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "faculty_id")
    private Long facultyId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "time_slot_id", nullable = false)
    private Long timeSlotId;

    @Column(name = "day_of_week", nullable = false, length = 10)
    private String dayOfWeek;

    /** LECTURE, LAB, TUTORIAL, EXAM */
    @Column(name = "entry_type", length = 20)
    private String entryType = "LECTURE";

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
