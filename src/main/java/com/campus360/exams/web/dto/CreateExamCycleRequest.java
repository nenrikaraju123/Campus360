package com.campus360.exams.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateExamCycleRequest(
        @NotBlank @Size(max = 200) String name,
        Long termId,
        @Size(max = 20) String academicYear,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}
