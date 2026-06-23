package com.campus360.iam.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(@Email @NotBlank String email, String tenantCode) {
}
