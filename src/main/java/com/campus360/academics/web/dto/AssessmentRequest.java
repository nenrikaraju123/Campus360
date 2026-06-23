package com.campus360.academics.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record AssessmentRequest(
        @NotNull Long sectionId,
        @NotBlank String title,
        String type,
        @NotNull @Positive BigDecimal maxMarks,
        BigDecimal weightagePct,
        Instant dueDate,
        String instructions) {
}
