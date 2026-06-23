package com.campus360.timetable.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTimetableTemplateRequest(
        @NotBlank @Size(max = 100) String name,
        Long termId,
        @Size(max = 20) String academicYear
) {}
