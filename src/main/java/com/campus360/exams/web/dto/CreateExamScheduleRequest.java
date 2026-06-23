package com.campus360.exams.web.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateExamScheduleRequest(
        @NotNull Long courseId,
        @NotNull LocalDate examDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        Long roomId,
        Long invigilatorId
) {}
