package com.campus360.placement.eligibility;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * Eligibility rules stored as JSON on a job posting, so placement officers can
 * define new rules without code changes. All fields are optional — a null field
 * means "no constraint on this dimension".
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EligibilityCriteria(
        BigDecimal minCgpa,
        List<String> branches,
        Integer maxBacklogs,
        Integer batchYear) {
}
