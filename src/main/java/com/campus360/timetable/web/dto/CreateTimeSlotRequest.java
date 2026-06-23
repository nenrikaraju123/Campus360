package com.campus360.timetable.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record CreateTimeSlotRequest(
        @NotBlank @Size(max = 10) String dayOfWeek,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @Size(max = 50) String slotLabel,
        Boolean isBreak,
        Integer displayOrder
) {}
