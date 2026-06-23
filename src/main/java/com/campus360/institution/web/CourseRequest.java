package com.campus360.institution.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CourseRequest(
        @NotNull Long departmentId,
        @NotBlank String code,
        @NotBlank String title,
        @PositiveOrZero int creditHours,
        String type,
        String description) {
}
