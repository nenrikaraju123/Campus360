package com.campus360.studentlife.web.dto;

import jakarta.validation.constraints.NotBlank;

public record GrievanceRequest(
        String category,
        @NotBlank String subject,
        @NotBlank String description,
        String priority) {
}
