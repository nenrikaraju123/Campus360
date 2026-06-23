package com.campus360.ai.service;

import java.util.List;

/** Placement readiness for a student: deterministic score + AI coaching. */
public record ReadinessReport(
        Long studentId,
        int score,
        String band,
        List<String> factors,
        String coaching,
        boolean aiLive) {
}
