package com.campus360.institution.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SectionRequest(
        @NotNull Long courseId,
        @NotNull Long termId,
        Long facultyUserId,
        @PositiveOrZero int capacity,
        String schedule) {
}
