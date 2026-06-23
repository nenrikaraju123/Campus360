package com.campus360.exams.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateExamComponentRequest(
        @NotNull Long courseId,
        @NotBlank @Size(max = 50) String componentName,
        @NotNull BigDecimal maxMarks,
        BigDecimal passingMarks,
        BigDecimal weightagePct
) {}
