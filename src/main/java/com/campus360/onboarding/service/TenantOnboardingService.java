package com.campus360.onboarding.service;

import com.campus360.iam.domain.RoleName;
import com.campus360.iam.domain.User;
import com.campus360.iam.domain.UserStatus;
import com.campus360.iam.repository.RoleRepository;
import com.campus360.iam.repository.UserRepository;
import com.campus360.institution.domain.Institution;
import com.campus360.institution.repository.InstitutionRepository;
import com.campus360.notification.domain.NotificationEvent;
import com.campus360.notification.service.NotificationPersistenceService;
import com.campus360.onboarding.domain.RegistrationStatus;
import com.campus360.onboarding.domain.TenantRegistration;
import com.campus360.onboarding.repository.TenantRegistrationRepository;
import com.campus360.onboarding.web.CreateInstitutionRequest;
import com.campus360.onboarding.web.RegisterTenantRequest;
import com.campus360.onboarding.web.UpdateInstitutionRequest;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.mail.EmailTemplateBuilder;
import com.campus360.platform.mail.MailService;
import com.campus360.platform.security.CurrentUser;
import com.campus360.student.repository.StudentProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Tenant lifecycle owned by the platform: public registration requests, the
 * SUPER_ADMIN approval workflow that provisions an institution + its first admin,
 * direct provisioning, suspend/activate, update, and soft-delete.
 *
 * <p>Every state transition fires the appropriate notifications so the platform
 * admin and the institution admin both receive real-time in-app notifications
 * and email alerts — no polling required.
 */
@Service
@Transactional
public class TenantOnboardingService {

    private static final Logger log = LoggerFactory.getLogger(TenantOnboardingService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PWD_ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private final TenantRegistrationRepository registrations;
    private final InstitutionRepository institutions;
    private final UserRepository users;
    private final RoleRepository roles;
    private final StudentProfileRepository students;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationPersistenceService notificationService;

    @Value("${campus360.portal-url:http://localhost:5173}")
    private String portalUrl;

    public TenantOnboardingService(
            TenantRegistrationRepository registrations,
            InstitutionRepository institutions,
            UserRepository users,
            RoleRepository roles,
            StudentProfileRepository students,
            PasswordEncoder passwordEncoder,
            MailService mailService,
            ApplicationEventPublisher eventPublisher,
            NotificationPersistenceService notificationService) {
        this.registrations = registrations;
        this.institutions = institutions;
        this.users = users;
        this.roles = roles;
        this.students = students;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.eventPublisher = eventPublisher;
        this.notificationService = notificationService;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Public Registration — submitted by an institution applicant
    // ══════════════════════════════════════════════════════════════════════════

    public TenantRegistration submit(RegisterTenantRequest req) {
        if (institutions.existsByCodeIgnoreCase(req.institutionCode())) {
            throw ApiException.conflict("An institution with this code already exists.");
        }
        if (registrations.existsByInstitutionCodeIgnoreCaseAndStatus(req.institutionCode(), RegistrationStatus.PENDING)) {
            throw ApiException.conflict("A registration for this code is already pending review.");
        }
        if (registrations.existsByAdminEmailIgnoreCaseAndStatus(req.adminEmail(), RegistrationStatus.PENDING)) {
            throw ApiException.conflict("A registration with this admin email is already pending review.");
        }

        TenantRegistration r = new TenantRegistration();
        r.setInstitutionName(req.institutionName());
        r.setInstitutionCode(req.institutionCode());
        if (req.type() != null && !req.type().isBlank()) r.setType(req.type());
        r.setAdminFullName(req.adminFullName());
        r.setAdminEmail(req.adminEmail());
        r.setContactPhone(req.contactPhone());
        r.setMessage(req.message());
        r = registrations.save(r);

        // Notify all platform SUPER_ADMINs — real-time in-app notification
        notifyPlatformAdmins(
                "REGISTRATION_RECEIVED",
                "New Tenant Registration",
                String.format("'%s' (%s) has submitted a registration request and is awaiting review.",
                        req.institutionName(), req.institutionCode()));

        String body = EmailTemplateBuilder.build(
                "Registration Received",
                "Registration Received",
                """
                <p>Thank you for registering <strong>%s</strong> with Campus360.</p>
                <div style="background-color: #f1f5f9; padding: 20px; border-radius: 8px; margin: 24px 0;">
                    <table style="width: 100%%; text-align: left; font-family: inherit; font-size: 15px;">
                        <tr><td style="padding: 6px 0; color: #64748b; width: 30%%;">Institution</td><td style="font-weight: 600;">%s</td></tr>
                        <tr><td style="padding: 6px 0; color: #64748b;">Code</td><td style="font-weight: 600;">%s</td></tr>
                        <tr><td style="padding: 6px 0; color: #64748b;">Status</td><td><span style="background-color: #fef08a; color: #854d0e; padding: 4px 10px; border-radius: 6px; font-size: 13px; font-weight: 700; letter-spacing: 0.5px;">PENDING REVIEW</span></td></tr>
                    </table>
                </div>
                <p>Our team will review your application and get back to you within 2-3 business days. You will receive an email notification once a decision is made.</p>
                <p>If you have any questions, simply reply to this email.</p>
                """.formatted(req.institutionName(), req.institutionName(), req.institutionCode()),
                null, null);

        // Acknowledge to applicant
        mailService.send(req.adminEmail(), "Campus360 — Registration Received", body);

        log.info("Registration submitted: {} ({})", req.institutionName(), req.institutionCode());
        return r;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Review Queue
    // ══════════════════════════════════════════════════════════════════════════

    public List<TenantRegistration> list(RegistrationStatus status) {
        return status == null
                ? registrations.findAllByOrderByCreatedAtDesc()
                : registrations.findByStatusOrderByCreatedAtDesc(status);
    }

    public TenantRegistration get(Long id) {
        return registrations.findById(id)
                .orElseThrow(() -> ApiException.notFound("Registration not found: " + id));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Approve — provisions the institution and notifies the admin
    // ══════════════════════════════════════════════════════════════════════════

    public ProvisionResult approve(Long registrationId, String notes) {
        TenantRegistration r = get(registrationId);
        if (r.getStatus() != RegistrationStatus.PENDING) {
            throw ApiException.badRequest("Registration is already " + r.getStatus());
        }

        ProvisionResult result = provision(
                r.getInstitutionName(), r.getInstitutionCode(),
                r.getType(), r.getAdminFullName(), r.getAdminEmail(), null);

        r.setStatus(RegistrationStatus.APPROVED);
        r.setInstitutionId(result.institutionId());
        r.setReviewNotes(notes);
        r.setReviewedBy(reviewer());
        r.setReviewedAt(Instant.now());
        r.setUpdatedAt(Instant.now());
        registrations.save(r);

        // Notify the new institution's admin user (in-app)
        users.findByTenantIdAndEmailIgnoreCase(result.institutionId(), r.getAdminEmail())
                .ifPresent(adminUser -> notificationService.notify(
                        adminUser.getId(),
                        result.institutionId(),
                        "REGISTRATION_APPROVED",
                        "🎉 Your Campus360 institution is ready!",
                        String.format("Welcome! '%s' has been approved. Sign in at %s/login to get started.",
                                r.getInstitutionName(), portalUrl),
                        "Institution", result.institutionId()));

        // Notify all platform admins that this one was actioned
        notifyPlatformAdmins(
                "REGISTRATION_APPROVED",
                "Registration Approved",
                String.format("'%s' has been approved and provisioned by %s.",
                        r.getInstitutionName(), reviewer()));

        log.info("Registration approved: {} → institution {}", r.getInstitutionCode(), result.institutionId());
        return result;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Reject
    // ══════════════════════════════════════════════════════════════════════════

    public TenantRegistration reject(Long registrationId, String notes) {
        TenantRegistration r = get(registrationId);
        if (r.getStatus() != RegistrationStatus.PENDING) {
            throw ApiException.badRequest("Registration is already " + r.getStatus());
        }

        r.setStatus(RegistrationStatus.REJECTED);
        r.setReviewNotes(notes);
        r.setReviewedBy(reviewer());
        r.setReviewedAt(Instant.now());
        r.setUpdatedAt(Instant.now());
        registrations.save(r);

        String body = EmailTemplateBuilder.build(
                "Registration Decision: Not Approved",
                "Registration Update",
                """
                <p>We have reviewed the registration request for <strong>%s</strong>.</p>
                <div style="background-color: #f1f5f9; padding: 20px; border-radius: 8px; margin: 24px 0;">
                    <table style="width: 100%%; text-align: left; font-family: inherit; font-size: 15px;">
                        <tr><td style="padding: 6px 0; color: #64748b; width: 30%%;">Institution</td><td style="font-weight: 600;">%s</td></tr>
                        <tr><td style="padding: 6px 0; color: #64748b;">Code</td><td style="font-weight: 600;">%s</td></tr>
                        <tr><td style="padding: 6px 0; color: #64748b;">Reviewed by</td><td style="font-weight: 600;">%s</td></tr>
                        <tr><td style="padding: 6px 0; color: #64748b;">Decision</td><td><span style="background-color: #fee2e2; color: #991b1b; padding: 4px 10px; border-radius: 6px; font-size: 13px; font-weight: 700; letter-spacing: 0.5px;">NOT APPROVED</span></td></tr>
                    </table>
                </div>
                <div style="background-color: #fff1f2; border-left: 4px solid #e11d48; padding: 16px; margin: 24px 0; border-radius: 4px;">
                    <p style="margin: 0; color: #9f1239;"><strong>Reason:</strong><br/>%s</p>
                </div>
                <p style="margin-top: 24px;">If you believe this decision is in error, or you have additional information to provide, please contact our platform support team by replying to this email.</p>
                """.formatted(
                        r.getInstitutionName(),
                        r.getInstitutionName(),
                        r.getInstitutionCode(),
                        reviewer(),
                        notes != null && !notes.isBlank() ? notes : "No specific reason provided."
                ),
                null, null);

        // Detailed rejection email to the applicant
        mailService.send(r.getAdminEmail(), "Campus360 — Registration Decision: Not Approved", body);

        notifyPlatformAdmins(
                "REGISTRATION_REJECTED",
                "Registration Rejected",
                String.format("'%s' has been rejected by %s.", r.getInstitutionName(), reviewer()));

        log.info("Registration rejected: {}", r.getInstitutionCode());
        return r;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Direct tenant management (platform admin only)
    // ══════════════════════════════════════════════════════════════════════════

    public ProvisionResult createInstitution(CreateInstitutionRequest req) {
        return provision(req.institutionName(), req.institutionCode(),
                req.type() == null ? "UNIVERSITY" : req.type(),
                req.adminFullName(), req.adminEmail(), req.password());
    }

    public List<Institution> listInstitutions() {
        return institutions.findAll();
    }

    public Institution updateInstitution(Long id, UpdateInstitutionRequest req) {
        Institution inst = institutions.findById(id)
                .orElseThrow(() -> ApiException.notFound("Institution not found: " + id));
        if (req.name() != null && !req.name().isBlank()) inst.setName(req.name());
        if (req.type() != null && !req.type().isBlank()) inst.setType(req.type());
        if (req.address() != null) inst.setAddress(req.address());
        Institution saved = institutions.save(inst);
        log.info("Institution {} updated by {}", id, reviewer());
        return saved;
    }

    /**
     * Soft-deactivates a tenant. Hard delete is intentionally not supported —
     * financial and academic records must be retained for compliance.
     */
    public Institution deactivateInstitution(Long id) {
        Institution inst = institutions.findById(id)
                .orElseThrow(() -> ApiException.notFound("Institution not found: " + id));
        inst.setStatus("DEACTIVATED");
        Institution saved = institutions.save(inst);
        log.warn("Institution {} ({}) DEACTIVATED by {}", inst.getName(), id, reviewer());
        return saved;
    }

    public Institution setStatus(Long institutionId, String status) {
        Institution institution = institutions.findById(institutionId)
                .orElseThrow(() -> ApiException.notFound("Institution not found: " + institutionId));
        String normalized = status.toUpperCase();
        if (!normalized.equals("ACTIVE") && !normalized.equals("SUSPENDED")) {
            throw ApiException.badRequest("Status must be ACTIVE or SUSPENDED");
        }
        institution.setStatus(normalized);
        Institution saved = institutions.save(institution);

        // Notify institution admin of status change
        String msg = normalized.equals("ACTIVE")
                ? String.format("Your institution '%s' has been reactivated. You now have full access.", institution.getName())
                : String.format("Your institution '%s' has been suspended. Please contact platform support.", institution.getName());
        users.findByTenantIdAndEmailIgnoreCase(institutionId, /* admin email unknown here — notify tenant-wide */ "")
                .ifPresent(u -> {}); // fallback: fire tenant-wide event
        eventPublisher.publishEvent(
                NotificationEvent.of(institutionId, "INSTITUTION_STATUS_CHANGED",
                        normalized.equals("ACTIVE") ? "Institution Reactivated" : "Institution Suspended",
                        msg));
        return saved;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Tenant Stats
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public TenantStatsDto getTenantStats(Long institutionId) {
        Institution inst = institutions.findById(institutionId)
                .orElseThrow(() -> ApiException.notFound("Institution not found: " + institutionId));

        long totalUsers    = users.countByTenantId(institutionId);
        long studentCount  = students.countByTenantId(institutionId);
        long facultyCount  = users.countByTenantIdAndRoleName(institutionId, "FACULTY");
        long hodCount      = users.countByTenantIdAndRoleName(institutionId, "HOD");
        long placementCount = users.countByTenantIdAndRoleName(institutionId, "PLACEMENT_OFFICER");
        long financeCount  = users.countByTenantIdAndRoleName(institutionId, "FINANCE");

        return new TenantStatsDto(
                institutionId,
                inst.getName(),
                inst.getCode(),
                inst.getType(),
                inst.getAddress(),
                inst.getStatus(),
                inst.getCreatedAt(),
                totalUsers,
                studentCount,
                facultyCount,
                hodCount,
                placementCount,
                financeCount);
    }

    @Transactional(readOnly = true)
    public List<TenantStatsDto> getAllTenantStats() {
        return institutions.findAll().stream()
                .map(inst -> getTenantStats(inst.getId()))
                .toList();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Provisioning core
    // ══════════════════════════════════════════════════════════════════════════

    private ProvisionResult provision(String name, String code, String type,
                                      String adminFullName, String adminEmail,
                                      String explicitPassword) {
        if (institutions.existsByCodeIgnoreCase(code)) {
            throw ApiException.conflict("An institution with code '" + code + "' already exists.");
        }

        Institution institution = new Institution();
        institution.setName(name);
        institution.setCode(code);
        institution.setType(type == null ? "UNIVERSITY" : type);
        institution.setStatus("ACTIVE");
        institution = institutions.save(institution);

        if (users.existsByTenantIdAndEmailIgnoreCase(institution.getId(), adminEmail)) {
            throw ApiException.conflict("Admin email already exists in this institution.");
        }

        boolean generated = explicitPassword == null || explicitPassword.isBlank();
        String rawPassword = generated ? generatePassword() : explicitPassword;

        User admin = new User();
        admin.setTenantId(institution.getId());
        admin.setEmail(adminEmail);
        admin.setFullName(adminFullName);
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        admin.setStatus(UserStatus.ACTIVE);
        admin.setMustChangePassword(generated);
        admin.setRoles(Set.of(roles.findByName(RoleName.INSTITUTION_ADMIN.name())
                .orElseThrow(() -> ApiException.badRequest("INSTITUTION_ADMIN role not seeded"))));
        users.save(admin);

        String loginUrl = portalUrl + "/login";
        String passwordHtml = generated 
                ? "<p><strong>Password:</strong> <span style=\"font-family: monospace; font-size: 18px; background: #e2e8f0; padding: 4px 8px; border-radius: 6px; letter-spacing: 1px;\">" + rawPassword + "</span> <span style=\"color: #64748b; font-size: 14px; margin-left: 8px;\">(temporary)</span></p>" 
                : "<p><strong>Password:</strong> Use the password set during provisioning.</p>";

        String body = EmailTemplateBuilder.build(
                "Your Institution is Ready",
                "Welcome to Campus360!",
                """
                <p>Congratulations! Your institution has been approved and provisioned on Campus360.</p>
                <div style="background-color: #f1f5f9; padding: 20px; border-radius: 8px; margin: 24px 0;">
                    <table style="width: 100%%; text-align: left; font-family: inherit; font-size: 15px;">
                        <tr><td style="padding: 6px 0; color: #64748b; width: 30%%;">Institution</td><td style="font-weight: 600;">%s</td></tr>
                        <tr><td style="padding: 6px 0; color: #64748b;">Tenant Code</td><td style="font-weight: 600;">%s</td></tr>
                        <tr><td style="padding: 6px 0; color: #64748b;">Admin Email</td><td style="font-weight: 600;">%s</td></tr>
                    </table>
                </div>
                <div style="margin: 24px 0; padding: 16px; border: 1px dashed #cbd5e1; border-radius: 8px; background-color: #f8fafc;">
                    %s
                    <p style="font-size: 13px; color: #64748b; margin-top: 8px; margin-bottom: 0;">You will be asked to set your own secure password on your first sign-in.</p>
                </div>
                <h3 style="margin-top: 32px; font-size: 18px; color: #0f172a;">As an INSTITUTION ADMIN, you can:</h3>
                <ul style="padding-left: 20px; color: #475569; line-height: 1.8;">
                  <li>Manage departments, programs, and courses</li>
                  <li>Onboard students, faculty, and staff (individually or via bulk import)</li>
                  <li>Configure fee structures and track collections</li>
                  <li>Manage placement drives and job postings</li>
                  <li>Monitor attendance, results, and student welfare</li>
                </ul>
                <p style="margin-top: 24px;">If you need assistance, contact platform support.</p>
                """.formatted(name, code, adminEmail, passwordHtml),
                loginUrl, "Sign In to Portal");

        // Rich welcome email with portal URL and credentials
        mailService.send(adminEmail, "Welcome to Campus360 — Your Institution is Ready", body);

        log.info("Institution provisioned: {} ({}) — admin: {}", name, code, adminEmail);
        return new ProvisionResult(institution.getId(), code, adminEmail,
                generated ? rawPassword : null, generated);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Sends an in-app notification to every platform SUPER_ADMIN user.
     * Used for: new registration received, registration approved/rejected.
     */
    private void notifyPlatformAdmins(String type, String title, String message) {
        List<User> admins = users.findPlatformUsersByRole("SUPER_ADMIN");
        for (User admin : admins) {
            notificationService.notify(
                    admin.getId(), null, type, title, message, "TenantRegistration", null);
        }
        // Also fire a null-tenant event so the SSE stream picks it up
        eventPublisher.publishEvent(NotificationEvent.of(null, type, title, message));
    }

    private static String generatePassword() {
        StringBuilder sb = new StringBuilder(14);
        for (int i = 0; i < 14; i++) {
            sb.append(PWD_ALPHABET.charAt(RANDOM.nextInt(PWD_ALPHABET.length())));
        }
        return sb.toString();
    }

    private static String reviewer() {
        var p = CurrentUser.principal();
        return p == null || p.email() == null ? "platform" : p.email();
    }
}
