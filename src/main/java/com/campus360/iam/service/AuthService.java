package com.campus360.iam.service;

import com.campus360.iam.domain.RefreshToken;
import com.campus360.iam.domain.Role;
import com.campus360.iam.domain.User;
import com.campus360.iam.domain.UserStatus;
import com.campus360.iam.repository.RefreshTokenRepository;
import com.campus360.iam.repository.UserRepository;
import com.campus360.iam.web.dto.LoginRequest;
import com.campus360.iam.web.dto.TokenResponse;
import com.campus360.institution.domain.Institution;
import com.campus360.institution.repository.InstitutionRepository;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.security.JwtProperties;
import com.campus360.platform.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final LoginAuditService loginAuditService;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       InstitutionRepository institutionRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, JwtProperties jwtProperties, LoginAuditService loginAuditService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.institutionRepository = institutionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.loginAuditService = loginAuditService;
    }

    /**
     * Tenant-scoped login. With a tenantCode we resolve the institution (which
     * must be ACTIVE) and look the user up within it; without one we authenticate
     * a platform SUPER_ADMIN. Failures are deliberately indistinguishable.
     */
    @Transactional
    public TokenResponse login(LoginRequest req) {
        User user = null;
        Long tenantId = null;
        try {
            if (req.tenantCode() == null || req.tenantCode().isBlank()) {
                user = userRepository.findByTenantIdIsNullAndEmailIgnoreCase(req.email())
                        .orElseThrow(AuthService::invalidCredentials);
            } else {
                Institution institution = institutionRepository.findByCodeIgnoreCase(req.tenantCode())
                        .orElseThrow(AuthService::invalidCredentials);
                requireActiveInstitution(institution);
                tenantId = institution.getId();
                user = userRepository.findByTenantIdAndEmailIgnoreCase(institution.getId(), req.email())
                        .orElseThrow(AuthService::invalidCredentials);
            }

            if (user.getStatus() != UserStatus.ACTIVE) {
                throw ApiException.forbidden("Account is not active");
            }
            if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
                throw invalidCredentials();
            }
            
            loginAuditService.logSuccess(user.getId(), req.email(), tenantId, getClientIp(), getUserAgent());
            return issueTokens(user);
        } catch (Exception e) {
            Long userId = (user != null) ? user.getId() : null;
            loginAuditService.logFailure(userId, req.email(), tenantId, getClientIp(), getUserAgent());
            throw e;
        }
    }

    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> ApiException.badRequest("Invalid refresh token"));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.badRequest("Refresh token expired or revoked");
        }
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> ApiException.badRequest("User no longer exists"));
        if (user.getTenantId() != null) {
            requireActiveInstitution(institutionRepository.findById(user.getTenantId())
                    .orElseThrow(AuthService::invalidCredentials));
        }
        return issueTokens(user);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.badRequest("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw ApiException.badRequest("Current password is incorrect");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw ApiException.badRequest("New password must be at least 8 characters");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
        // Force re-login everywhere else.
        refreshTokenRepository.revokeAllForUser(userId);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(sha256(rawRefreshToken)).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    private TokenResponse issueTokens(User user) {
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getTenantId(), roles);

        String rawRefresh = UUID.randomUUID().toString() + UUID.randomUUID();
        RefreshToken rt = new RefreshToken();
        rt.setUserId(user.getId());
        rt.setTokenHash(sha256(rawRefresh));
        rt.setExpiresAt(Instant.now().plus(jwtProperties.getRefreshTokenDays(), ChronoUnit.DAYS));
        refreshTokenRepository.save(rt);

        return new TokenResponse(access, rawRefresh, "Bearer",
                jwtProperties.getAccessTokenMinutes() * 60, user.getId(), user.getTenantId(),
                roles, user.isMustChangePassword());
    }

    private void requireActiveInstitution(Institution institution) {
        if (!"ACTIVE".equalsIgnoreCase(institution.getStatus())) {
            throw ApiException.forbidden("This institution is not active. Contact the platform administrator.");
        }
    }

    private static ApiException invalidCredentials() {
        return ApiException.unauthorized("Invalid credentials");
    }

    private static String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private String getClientIp() {
        org.springframework.web.context.request.RequestAttributes attrs = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attrs instanceof org.springframework.web.context.request.ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest().getRemoteAddr();
        }
        return null;
    }

    private String getUserAgent() {
        org.springframework.web.context.request.RequestAttributes attrs = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attrs instanceof org.springframework.web.context.request.ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest().getHeader("User-Agent");
        }
        return null;
    }
}
