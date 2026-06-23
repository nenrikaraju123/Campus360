package com.campus360.academics.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateHolidayRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull LocalDate holidayDate,
        String holidayType,
        Boolean isOptional
) {}
