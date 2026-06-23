package com.campus360.institution.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProgramRequest(
        @NotNull Long departmentId,
        @NotBlank String name,
        @NotBlank String code,
        String level,
        @PositiveOrZero int durationTerms,
        @PositiveOrZero int totalCredits) {
}
