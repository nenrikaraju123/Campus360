package com.campus360.faculty.web.dto;

import jakarta.validation.constraints.NotNull;

public record FacultyCourseAssignmentRequest(
        @NotNull Long sectionId,
        @NotNull Long courseId,
        Long termId,
        String academicYear
) {}
