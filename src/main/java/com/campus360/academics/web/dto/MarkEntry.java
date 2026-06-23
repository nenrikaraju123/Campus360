package com.campus360.academics.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record MarkEntry(
        @NotNull Long enrollmentId,
        @NotNull @PositiveOrZero BigDecimal score,
        String remarks) {
}
