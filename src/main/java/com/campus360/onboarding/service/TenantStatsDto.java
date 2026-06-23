package com.campus360.onboarding.service;

import java.time.Instant;

/**
 * Snapshot of a tenant's platform-level usage metrics.
 * Returned by GET /api/v1/platform/institutions/{id}/stats.
 */
public record TenantStatsDto(
        Long institutionId,
        String name,
        String code,
        String type,
        String address,
        String status,
        Instant createdAt,
        long totalUsers,
        long studentCount,
        long facultyCount,
        long hodCount,
        long placementOfficerCount,
        long financeCount) {
}
