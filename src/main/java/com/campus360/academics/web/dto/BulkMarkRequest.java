package com.campus360.academics.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Bulk grade entry for an assessment. */
public record BulkMarkRequest(
        @NotNull Long assessmentId,
        @NotEmpty List<MarkEntry> marks) {
}
