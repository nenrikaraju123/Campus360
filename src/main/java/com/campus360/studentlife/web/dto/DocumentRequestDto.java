package com.campus360.studentlife.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentRequestDto(
        @NotNull Long studentId,
        @NotBlank String docType,
        String purpose,
        int copies) {
}
