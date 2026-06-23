package com.campus360.timetable.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTimetableEntryRequest(
        @NotNull Long sectionId,
        @NotNull Long courseId,
        Long facultyId,
        Long roomId,
        @NotNull Long timeSlotId,
        @NotBlank @Size(max = 10) String dayOfWeek,
        @Size(max = 20) String entryType
) {}
