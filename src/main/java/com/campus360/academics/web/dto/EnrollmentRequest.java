package com.campus360.academics.web.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollmentRequest(
        @NotNull Long studentId,
        @NotNull Long sectionId,
        @NotNull Long termId) {
}
