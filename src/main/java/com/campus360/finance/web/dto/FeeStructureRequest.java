package com.campus360.finance.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeStructureRequest(
        Long programId,
        Long termId,
        @NotBlank String name,
        @NotNull @Positive BigDecimal amount,
        String feeType,
        LocalDate dueDate) {
}
