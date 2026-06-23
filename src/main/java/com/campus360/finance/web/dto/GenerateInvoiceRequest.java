package com.campus360.finance.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record GenerateInvoiceRequest(
        @NotNull Long studentId,
        @NotNull Long feeStructureId,
        LocalDate dueDate) {
}
