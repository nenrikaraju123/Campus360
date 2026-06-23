package com.campus360.academics.web.dto;

import jakarta.validation.constraints.NotNull;

/** Single attendance mark within a bulk request. */
public record AttendanceEntry(
        @NotNull Long enrollmentId,
        @NotNull String status) {
}
