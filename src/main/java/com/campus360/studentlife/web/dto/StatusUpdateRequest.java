package com.campus360.studentlife.web.dto;

import jakarta.validation.constraints.NotBlank;

public record StatusUpdateRequest(@NotBlank String status, String resolution) {
}
