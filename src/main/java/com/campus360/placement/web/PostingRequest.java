package com.campus360.placement.web;

import com.campus360.placement.eligibility.EligibilityCriteria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record PostingRequest(
        @NotNull Long companyId,
        @NotBlank String title,
        String type,
        BigDecimal ctc,
        String location,
        String description,
        EligibilityCriteria eligibility,
        Instant closesAt) {
}
