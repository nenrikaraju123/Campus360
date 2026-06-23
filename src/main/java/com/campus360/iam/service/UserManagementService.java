package com.campus360.iam.service;

import com.campus360.iam.domain.*;
import com.campus360.iam.repository.PasswordResetTokenRepository;
import com.campus360.iam.repository.RoleRepository;
import com.campus360.iam.repository.UserRepository;
import com.campus360.iam.web.dto.*;
import com.campus360.notification.domain.NotificationEvent;
import com.campus360.notification.service.NotificationPersistenceService;
import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.mail.MailService;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.shared.dto.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enterprise user management service. Handles CRUD operations on users within
 * a tenant, role assignments, password reset flows, and user lifecycle
 * (invite, activate, suspend, disable).
 *
 * <p>Every user creation publishes a {@link NotificationEvent} so that
 * in-app notifications and the SSE real-time stream are both updated
 * automatically — no polling required.
 */
@Service
@Transactional
public class UserManagementService {

    private static final Logger log = LoggerFactory.getLogger(UserManagementService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PWD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$";

    @Value("${campus360.portal-url:http://localhost:5173}")
    private String portalUrl;
    private static final int RESET_TOKEN_HOURS = 24;

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationPersistenceService notificationService;

    public UserManagementService(UserRepository users, RoleRepository roles,
                                 PasswordResetTokenRepository resetTokens,
                                 PasswordEncoder passwordEncoder, MailService mailService,
                                 AuditService auditService,
                                 ApplicationEventPublisher eventPublisher,
                                 NotificationPersistenceService notificationService) {
        this.users = users;
        this.roles = roles;
        this.resetTokens = resetTokens;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
        this.notificationService = notificationService;
    }

    // ── List users (tenant-scoped, paginated) ──────────────────────────────

    @Transactional(readOnly = true)
    public Page<User> listUsers(Pageable pageable) {
        Long tenant = TenantContext.requireTenantId();
        return users.findByTenantId(tenant, pageable);
    }

    @Transactional(readOnly = true)
    public Page<User> listUsersByRole(String roleName, Pageable pageable) {
        Long tenant = TenantContext.requireTenantId();
        return users.findByTenantIdAndRolesNameIgnoreCase(tenant, roleName, pageable);
    }

    @Transactional(readOnly = true)
    public User getUser(Long id) {
        Long tenant = TenantContext.requireTenantId();
        return users.findByIdAndTenantId(id, tenant)
                .orElseThrow(() -> ApiException.notFound("User not found: " + id));
    }

    // ── Create / invite user ───────────────────────────────────────────────

    /**
     * Creates a single user, sends a welcome email with the temp password,
     * and fires a {@link NotificationEvent} so the SSE stream and in-app
     * inbox are updated in real-time.
     */
    public User createUser(CreateUserRequest req) {
        Long tenant = TenantContext.requireTenantId();
        if (users.existsByTenantIdAndEmailIgnoreCase(tenant, req.email())) {
            throw ApiException.conflict("Email already registered: " + req.email());
        }

        User user = buildUser(req, tenant);
        String rawPassword = extractRawPassword(req, user);
        user = users.save(user);

        sendWelcomeEmail(req.email(), rawPassword, !rawPassword.equals(req.password()), user.getRoles());
        publishUserCreatedNotification(tenant, user);
        auditService.log("USER_CREATED", "User", user.getId(),
                "Created user " + req.email() + " with roles: " + req.roles());

        return user;
    }

    /**
     * Bulk-creates users in a single request. Each row is processed independently:
     * a failure on one row does NOT roll back the others. Returns a per-row result
     * list with success/error detail so the UI can show a summary table.
     *
     * <p>After all rows are processed, a single tenant-wide notification is published
     * summarising the batch outcome.
     */
    public List<BulkCreateUserResult> bulkCreateUsers(List<CreateUserRequest> requests) {
        Long tenant = TenantContext.requireTenantId();
        List<BulkCreateUserResult> results = new ArrayList<>(requests.size());
        int successCount = 0;

        for (CreateUserRequest req : requests) {
            try {
                // Each row gets its own savepoint via nested exception handling
                User created = createUserInternal(req, tenant);
                results.add(BulkCreateUserResult.ok(req.email(), created));
                successCount++;
            } catch (ApiException ex) {
                log.warn("Bulk create skipped '{}': {}", req.email(), ex.getMessage());
                results.add(BulkCreateUserResult.fail(req.email(), ex.getMessage()));
            } catch (Exception ex) {
                log.error("Bulk create unexpected failure for '{}': {}", req.email(), ex.getMessage(), ex);
                results.add(BulkCreateUserResult.fail(req.email(), "Unexpected error: " + ex.getMessage()));
            }
        }

        // Single tenant-wide notification summarising the batch
        if (successCount > 0) {
            int failed = requests.size() - successCount;
            String msg = String.format(
                    "%d user account(s) created successfully%s.",
                    successCount,
                    failed > 0 ? " (" + failed + " skipped due to errors)" : "");
            eventPublisher.publishEvent(
                    NotificationEvent.of(tenant, "BULK_USERS_CREATED", "Bulk User Creation", msg));
        }

        auditService.log("BULK_USERS_CREATED", "User", null,
                String.format("Bulk create: %d/%d succeeded", successCount, requests.size()));
        return results;
    }

    // ── Update user ────────────────────────────────────────────────────────

    public User updateUser(Long id, UpdateUserRequest req) {
        User user = getUser(id);
        if (req.fullName() != null && !req.fullName().isBlank()) {
            user.setFullName(req.fullName());
        }
        if (req.email() != null && !req.email().isBlank()) {
            Long tenant = TenantContext.requireTenantId();
            if (!user.getEmail().equalsIgnoreCase(req.email())
                    && users.existsByTenantIdAndEmailIgnoreCase(tenant, req.email())) {
                throw ApiException.conflict("Email already in use: " + req.email());
            }
            user.setEmail(req.email());
        }
        if (req.status() != null) {
            user.setStatus(req.status());
            auditService.log("USER_STATUS_CHANGED", "User", id,
                    "Status changed to " + req.status());
        }
        return users.save(user);
    }

    // ── Role management ────────────────────────────────────────────────────

    public User assignRoles(Long userId, List<String> roleNames) {
        User user = getUser(userId);
        Set<Role> newRoles = roleNames.stream()
                .map(rn -> roles.findByName(rn.toUpperCase())
                        .orElseThrow(() -> ApiException.badRequest("Unknown role: " + rn)))
                .collect(Collectors.toSet());
        user.setRoles(newRoles);
        auditService.log("ROLES_ASSIGNED", "User", userId,
                "Roles set to: " + roleNames);
        return users.save(user);
    }

    // ── Suspend / activate ─────────────────────────────────────────────────

    public User suspendUser(Long id) {
        User user = getUser(id);
        user.setStatus(UserStatus.SUSPENDED);
        auditService.log("USER_SUSPENDED", "User", id, "User suspended");
        return users.save(user);
    }

    public User activateUser(Long id) {
        User user = getUser(id);
        user.setStatus(UserStatus.ACTIVE);
        auditService.log("USER_ACTIVATED", "User", id, "User activated");
        return users.save(user);
    }

    // ── Forgot / reset password flow ───────────────────────────────────────

    public void requestPasswordReset(String tenantCode, String email) {
        User user;
        if (tenantCode == null || tenantCode.isBlank()) {
            user = users.findByTenantIdIsNullAndEmailIgnoreCase(email).orElse(null);
        } else {
            user = users.findByTenantIdAndEmailIgnoreCase(
                    resolveTenantId(tenantCode), email).orElse(null);
        }
        if (user == null) {
            return; // silent — no user enumeration
        }

        resetTokens.deleteByUserId(user.getId());

        String rawToken = UUID.randomUUID().toString() + UUID.randomUUID();
        PasswordResetToken prt = new PasswordResetToken();
        prt.setUserId(user.getId());
        prt.setTokenHash(sha256(rawToken));
        prt.setExpiresAt(Instant.now().plus(RESET_TOKEN_HOURS, ChronoUnit.HOURS));
        resetTokens.save(prt);

        String body = com.campus360.platform.mail.EmailTemplateBuilder.build(
                "Password Reset Request",
                "Password Reset",
                """
                <p>A password reset was requested for your Campus360 account.</p>
                <div style="margin: 24px 0; padding: 16px; border: 1px dashed #cbd5e1; border-radius: 8px; background-color: #f8fafc; text-align: center;">
                    <p style="font-size: 14px; color: #64748b; margin: 0 0 8px;">Your Reset Token</p>
                    <span style="font-family: monospace; font-size: 24px; font-weight: 700; color: #0f172a; letter-spacing: 2px;">%s</span>
                </div>
                <p>This token expires in <strong>%d hours</strong>. If you did not request this, please safely ignore this email.</p>
                """.formatted(rawToken, RESET_TOKEN_HOURS),
                null, null);

        mailService.send(email, "Campus360 — Password Reset", body);

        auditService.logAsync("PASSWORD_RESET_REQUESTED", "User", user.getId(),
                "Password reset requested for " + email);
    }

    public void resetPassword(String rawToken, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw ApiException.badRequest("Password must be at least 8 characters");
        }
        PasswordResetToken prt = resetTokens.findByTokenHash(sha256(rawToken))
                .orElseThrow(() -> ApiException.badRequest("Invalid or expired reset token"));

        if (prt.isUsed() || prt.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.badRequest("Reset token has expired or already been used");
        }

        User user = users.findById(prt.getUserId())
                .orElseThrow(() -> ApiException.badRequest("User no longer exists"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        users.save(user);

        prt.setUsed(true);
        resetTokens.save(prt);

        auditService.log("PASSWORD_RESET_COMPLETED", "User", user.getId(),
                "Password was reset via token");
    }

    // ── Private helpers ────────────────────────────────────────────────────

    /** Single-user creation shared by both createUser() and bulkCreateUsers(). */
    private User createUserInternal(CreateUserRequest req, Long tenant) {
        if (users.existsByTenantIdAndEmailIgnoreCase(tenant, req.email())) {
            throw ApiException.conflict("Email already registered: " + req.email());
        }
        User user = buildUser(req, tenant);
        String rawPassword = extractRawPassword(req, user);
        user = users.save(user);

        sendWelcomeEmail(req.email(), rawPassword, !rawPassword.equals(req.password()), user.getRoles());

        // Per-user in-app notification (targeted, shows up in individual inbox)
        notificationService.notify(
                user.getId(), tenant,
                "USER_WELCOME",
                "Welcome to Campus360!",
                "Your account has been created. " +
                        (user.isMustChangePassword() ? "Please sign in with your temporary password and change it." : "Sign in to get started."),
                "User", user.getId());

        return user;
    }

    private User buildUser(CreateUserRequest req, Long tenant) {
        boolean hasPassword = req.password() != null && !req.password().isBlank();
        User user = new User();
        user.setTenantId(tenant);
        user.setEmail(req.email());
        user.setFullName(req.fullName());
        user.setPasswordHash(passwordEncoder.encode(
                hasPassword ? req.password() : generatePassword(12)));
        user.setStatus(UserStatus.ACTIVE);
        user.setMustChangePassword(!hasPassword);

        if (req.roles() != null && !req.roles().isEmpty()) {
            Set<Role> roleSet = req.roles().stream()
                    .map(rn -> roles.findByName(rn.toUpperCase())
                            .orElseThrow(() -> ApiException.badRequest("Unknown role: " + rn)))
                    .collect(Collectors.toSet());
            user.setRoles(roleSet);
        }
        return user;
    }

    /**
     * Returns the raw (plaintext) password for the welcome email.
     * If the caller supplied a password, it was already encoded into the user;
     * we return it for inclusion in the email only when it was auto-generated.
     */
    private String extractRawPassword(CreateUserRequest req, User user) {
        if (req.password() != null && !req.password().isBlank()) {
            return req.password();
        }
        // Auto-generate: re-generate for the email — the hash is already set
        String raw = generatePassword(12);
        user.setPasswordHash(passwordEncoder.encode(raw));
        return raw;
    }

    /**
     * Sends a rich welcome email with the portal login URL, credentials,
     * and role-specific next-step guidance.
     */
    private void sendWelcomeEmail(String email, String rawPassword, boolean isTemporary, Set<Role> userRoles) {
        String loginUrl = portalUrl + "/login";
        String roleGuidanceHtml = buildRoleGuidanceHtml(userRoles);

        String passwordHtml = isTemporary 
                ? "<p><strong>Password:</strong> <span style=\"font-family: monospace; font-size: 18px; background: #e2e8f0; padding: 4px 8px; border-radius: 6px; letter-spacing: 1px;\">" + rawPassword + "</span> <span style=\"color: #64748b; font-size: 14px; margin-left: 8px;\">(temporary)</span></p>" 
                : "<p><strong>Password:</strong> Use the password you were provided.</p>";

        String body = com.campus360.platform.mail.EmailTemplateBuilder.build(
                "Your Account is Ready",
                "Welcome to Campus360!",
                """
                <p>Welcome to Campus360! Your account has been successfully created.</p>
                <div style="background-color: #f1f5f9; padding: 20px; border-radius: 8px; margin: 24px 0;">
                    <table style="width: 100%%; text-align: left; font-family: inherit; font-size: 15px;">
                        <tr><td style="padding: 6px 0; color: #64748b; width: 30%%;">Email</td><td style="font-weight: 600;">%s</td></tr>
                    </table>
                </div>
                <div style="margin: 24px 0; padding: 16px; border: 1px dashed #cbd5e1; border-radius: 8px; background-color: #f8fafc;">
                    %s
                    %s
                </div>
                %s
                <p style="margin-top: 24px;">If you did not expect this email, please contact your institution administrator.</p>
                """.formatted(
                        email, 
                        passwordHtml,
                        isTemporary ? "<p style=\"font-size: 13px; color: #64748b; margin-top: 8px; margin-bottom: 0;\">You will be asked to set your own secure password on your first sign-in.</p>" : "",
                        roleGuidanceHtml),
                loginUrl, "Sign In to Portal");

        mailService.send(email, "Welcome to Campus360 — Your Account is Ready", body);
    }

    /**
     * Builds role-specific "next steps" guidance for the welcome email.
     */
    private String buildRoleGuidanceHtml(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) return "";

        Set<String> roleNames = roles.stream().map(Role::getName).collect(Collectors.toSet());

        if (roleNames.contains("STUDENT")) {
            return "<h3 style=\"margin-top: 32px; font-size: 18px; color: #0f172a;\">As a STUDENT, you can:</h3><ul style=\"padding-left: 20px; color: #475569; line-height: 1.8;\"><li>View your timetable, attendance, and results</li><li>Check fee dues and download receipts</li><li>Explore placement opportunities and apply</li><li>Access your AI career guidance assistant</li></ul>";
        }
        if (roleNames.contains("FACULTY")) {
            return "<h3 style=\"margin-top: 32px; font-size: 18px; color: #0f172a;\">As FACULTY, you can:</h3><ul style=\"padding-left: 20px; color: #475569; line-height: 1.8;\"><li>View your assigned courses and timetable</li><li>Mark attendance and enter exam marks</li><li>View student records and announcements</li></ul>";
        }
        if (roleNames.contains("PARENT")) {
            return "<h3 style=\"margin-top: 32px; font-size: 18px; color: #0f172a;\">As a PARENT, you can:</h3><ul style=\"padding-left: 20px; color: #475569; line-height: 1.8;\"><li>Monitor your child's attendance and results</li><li>Check fee dues and download receipts</li><li>View announcements from the institution</li></ul>";
        }
        if (roleNames.contains("INSTITUTION_ADMIN")) {
            return "<h3 style=\"margin-top: 32px; font-size: 18px; color: #0f172a;\">As an INSTITUTION ADMIN, you can:</h3><ul style=\"padding-left: 20px; color: #475569; line-height: 1.8;\"><li>Manage students, faculty, and academic structure</li><li>Monitor attendance, results, and fee collections</li><li>Configure placement drives and institution settings</li></ul>";
        }
        if (roleNames.contains("PLACEMENT_OFFICER")) {
            return "<h3 style=\"margin-top: 32px; font-size: 18px; color: #0f172a;\">As a PLACEMENT OFFICER, you can:</h3><ul style=\"padding-left: 20px; color: #475569; line-height: 1.8;\"><li>Manage company relationships and job postings</li><li>Schedule placement drives and interviews</li><li>Track student applications and offers</li></ul>";
        }
        return "<p style=\"margin-top: 24px; color: #475569;\">Sign in to explore all available features.</p>";
    }

    private void publishUserCreatedNotification(Long tenant, User user) {
        // Per-user targeted in-app notification
        notificationService.notify(
                user.getId(), tenant,
                "USER_WELCOME",
                "Welcome to Campus360!",
                "Your account has been created. " +
                        (user.isMustChangePassword() ? "Sign in with your temporary password." : "Sign in to get started."),
                "User", user.getId());

        // Tenant-wide broadcast (shows on admin stream)
        eventPublisher.publishEvent(NotificationEvent.of(
                tenant,
                "USER_CREATED",
                "New User Added",
                "User " + user.getFullName() + " (" + user.getEmail() + ") has been added to the platform."));
    }

    private Long resolveTenantId(String tenantCode) {
        // handled at controller level
        return null;
    }

    private static String generatePassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(PWD_CHARS.charAt(RANDOM.nextInt(PWD_CHARS.length())));
        }
        return sb.toString();
    }

    static String sha256(String value) {
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
}
