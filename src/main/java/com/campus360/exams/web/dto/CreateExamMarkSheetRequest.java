package com.campus360.exams.web.dto;

import jakarta.validation.constraints.NotNull;

public record CreateExamMarkSheetRequest(
        @NotNull Long courseId,
        @NotNull Long sectionId,
        @NotNull Long facultyId
) {}
