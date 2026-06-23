package com.campus360.ai.service;

import java.util.List;

/** Explains how well a student fits a posting: eligibility + AI rationale. */
public record JobFitReport(
        Long studentId,
        Long postingId,
        boolean eligible,
        List<String> eligibilityGaps,
        String explanation,
        boolean aiLive) {
}
