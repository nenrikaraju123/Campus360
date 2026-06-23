package com.campus360.iam.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Tenant-scoped login. {@code tenantCode} identifies the institution; leave it
 * null/blank only for platform SUPER_ADMIN accounts.
 */
public record LoginRequest(
        String tenantCode,
        @NotBlank @Email String email,
        @NotBlank String password) {
}
