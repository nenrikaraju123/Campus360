package com.campus360.academics.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Bulk attendance marking for a class meeting. */
public record BulkAttendanceRequest(
        @NotNull Long meetingId,
        @NotEmpty List<AttendanceEntry> records,
        String source) {
}
