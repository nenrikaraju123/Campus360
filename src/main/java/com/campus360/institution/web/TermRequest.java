package com.campus360.institution.web;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record TermRequest(
        @NotBlank String name,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate addDropEnd,
        String status) {
}
