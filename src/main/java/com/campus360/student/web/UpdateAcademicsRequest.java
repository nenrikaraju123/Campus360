package com.campus360.student.web;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateAcademicsRequest(
        @PositiveOrZero BigDecimal cgpa,
        @PositiveOrZero Integer activeBacklogs,
        Integer currentTerm) {
}
