package com.campus360.studentlife.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record LeaveRequestDto(
        @NotNull Long studentId,
        Long sectionId,
        String leaveType,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String reason) {
}
