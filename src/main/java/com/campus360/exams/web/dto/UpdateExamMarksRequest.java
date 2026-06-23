package com.campus360.exams.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record UpdateExamMarksRequest(
        @NotNull List<ExamMarkDto> marks
) {
    public record ExamMarkDto(
            @NotNull Long studentId,
            @NotNull Long examComponentId,
            BigDecimal marksObtained,
            Boolean isAbsent,
            @Size(max = 200) String remarks
    ) {}
}
