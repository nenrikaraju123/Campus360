package com.campus360.onboarding.service;

/** Outcome of provisioning a tenant — includes the one-time temp password. */
public record ProvisionResult(
        Long institutionId,
        String institutionCode,
        String adminEmail,
        String temporaryPassword,
        boolean mustChangePassword) {
}
