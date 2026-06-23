package com.campus360.iam.web.dto;

import java.util.List;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        Long userId,
        Long tenantId,
        List<String> roles,
        boolean mustChangePassword) {
}
