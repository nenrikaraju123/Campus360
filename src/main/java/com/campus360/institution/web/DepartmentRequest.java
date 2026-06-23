package com.campus360.institution.web;

import jakarta.validation.constraints.NotBlank;

public record DepartmentRequest(
        @NotBlank String name,
        @NotBlank String code,
        Long hodUserId) {
}
