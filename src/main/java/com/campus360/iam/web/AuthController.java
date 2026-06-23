package com.campus360.iam.web;

import com.campus360.iam.service.AuthService;
import com.campus360.iam.service.UserManagementService;
import com.campus360.iam.web.dto.*;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Tenant-scoped login, token lifecycle, password reset")
public class AuthController {

    private final AuthService authService;
    private final UserManagementService userManagementService;

    public AuthController(AuthService authService, UserManagementService userManagementService) {
        this.authService = authService;
        this.userManagementService = userManagementService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate (tenantCode + email for institution users; email only for platform admin)")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate the refresh token for a fresh access token")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refresh(req.refreshToken());
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke a refresh token")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req) {
        authService.logout(req.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change the authenticated user's password (required on first admin login)")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        Long userId = CurrentUser.id();
        if (userId == null) {
            throw ApiException.forbidden("Authentication required");
        }
        authService.changePassword(userId, req.currentPassword(), req.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset email (public, no user enumeration)")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody PasswordResetRequest req) {
        userManagementService.requestPasswordReset(req.tenantCode(), req.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using the token from the email")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetConfirm req) {
        userManagementService.resetPassword(req.token(), req.newPassword());
        return ResponseEntity.noContent().build();
    }
}
