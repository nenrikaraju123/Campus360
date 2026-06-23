package com.campus360.academics.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ClassMeetingRequest(
        @NotNull Long sectionId,
        @NotNull LocalDate meetingDate,
        String topic) {
}
