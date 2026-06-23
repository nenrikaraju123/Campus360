package com.campus360.timetable.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "time_slots")
@Getter
@Setter
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /** MONDAY, TUESDAY, etc. */
    @Column(name = "day_of_week", nullable = false, length = 10)
    private String dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_label", length = 50)
    private String slotLabel;

    @Column(name = "is_break")
    private Boolean isBreak = false;

    @Column(name = "display_order")
    private Integer displayOrder = 0;
}
