package com.campus360.academics.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCalendarEventRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description,
        @NotBlank @Size(max = 30) String eventType,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        String scope,
        Long scopeId,
        Boolean isAllDay
) {}
