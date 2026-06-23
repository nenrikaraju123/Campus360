package com.campus360.exams.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "exam_schedules")
@Getter
@Setter
public class ExamSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "exam_cycle_id", nullable = false)
    private Long examCycleId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "invigilator_id")
    private Long invigilatorId;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
