package com.campus360.onboarding.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Public application to onboard a new institution. */
public record RegisterTenantRequest(
        @NotBlank @Size(max = 200) String institutionName,
        @NotBlank @Size(max = 40) String institutionCode,
        @Size(max = 40) String type,
        @NotBlank @Size(max = 160) String adminFullName,
        @NotBlank @Email @Size(max = 180) String adminEmail,
        @Size(max = 40) String contactPhone,
        @Size(max = 1000) String message) {
}
