package com.campus360.placement.service;

import java.math.BigDecimal;

public record PlacementStats(
        long totalStudents,
        long placedStudents,
        double placementRatePct,
        BigDecimal highestCtc,
        BigDecimal averageCtc,
        long openPostings,
        long totalOffers) {
}
