package com.campus360.student.service;

import com.campus360.iam.domain.RoleName;
import com.campus360.iam.domain.User;
import com.campus360.iam.domain.UserStatus;
import com.campus360.iam.repository.RoleRepository;
import com.campus360.iam.repository.UserRepository;
import com.campus360.notification.domain.NotificationEvent;
import com.campus360.notification.service.NotificationPersistenceService;
import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.mail.MailService;
import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.student.domain.StudentProfile;
import com.campus360.student.repository.StudentProfileRepository;
import com.campus360.student.web.BulkCreateStudentResult;
import com.campus360.student.web.BulkStudentRequest;
import com.campus360.student.web.CreateStudentRequest;
import com.campus360.student.web.UpdateAcademicsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PWD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$";

    private final StudentProfileRepository students;
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationPersistenceService notificationService;

    @Value("${campus360.portal-url:http://localhost:5173}")
    private String portalUrl;

    public StudentService(StudentProfileRepository students, UserRepository users,
                          RoleRepository roles, PasswordEncoder passwordEncoder,
                          MailService mailService, AuditService auditService,
                          ApplicationEventPublisher eventPublisher,
                          NotificationPersistenceService notificationService) {
        this.students = students;
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
        this.notificationService = notificationService;
    }

    // ── Single student creation ──────────────────────────────────────────

    /** Creates a login (STUDENT role) and the linked academic profile in one step. */
    public StudentProfile createStudent(CreateStudentRequest req) {
        Long tenant = TenantContext.requireTenantId();
        if (users.existsByTenantIdAndEmailIgnoreCase(tenant, req.email())) {
            throw ApiException.conflict("Email already registered in this institution: " + req.email());
        }
        if (students.existsByTenantIdAndRollNumberIgnoreCase(tenant, req.rollNumber())) {
            throw ApiException.conflict("Roll number already exists: " + req.rollNumber());
        }

        String rawPassword = req.password();
        User user = new User();
        user.setTenantId(tenant);
        user.setEmail(req.email());
        user.setFullName(req.fullName());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setStatus(UserStatus.ACTIVE);
        user.setMustChangePassword(true);
        user.setRoles(Set.of(roles.findByName(RoleName.STUDENT.name())
                .orElseThrow(() -> ApiException.badRequest("STUDENT role not seeded"))));
        user = users.save(user);

        StudentProfile profile = new StudentProfile();
        profile.setTenantId(tenant);
        profile.setUserId(user.getId());
        profile.setRollNumber(req.rollNumber());
        profile.setBranch(req.branch());
        profile.setBatchYear(req.batchYear());
        profile.setProgramId(req.programId());
        profile.setAdmissionDate(req.admissionDate());
        profile = students.save(profile);

        // Send welcome email with portal URL and credentials
        sendStudentWelcomeEmail(req.email(), rawPassword);

        // In-app notification for the student
        notificationService.notify(
                user.getId(), tenant,
                "STUDENT_WELCOME",
                "Welcome to Campus360!",
                "Your student account is ready. Sign in to view your timetable, attendance, and more.",
                "StudentProfile", profile.getId());

        auditService.log("STUDENT_CREATED", "StudentProfile", profile.getId(),
                "Created student " + req.email() + " (Roll: " + req.rollNumber() + ")");

        return profile;
    }

    // ── Bulk student creation ────────────────────────────────────────────

    /**
     * Bulk-creates students in a single request. Each row is processed independently:
     * a failure on one row does NOT roll back the others. Returns a per-row result
     * list with success/error detail so the UI can show a summary table.
     *
     * <p>Every successfully created student receives a welcome email with the
     * portal URL, their login credentials, and role-specific guidance.
     */
    public List<BulkCreateStudentResult> bulkCreateStudents(List<BulkStudentRequest> requests) {
        Long tenant = TenantContext.requireTenantId();
        List<BulkCreateStudentResult> results = new ArrayList<>(requests.size());
        int successCount = 0;

        for (BulkStudentRequest req : requests) {
            try {
                StudentProfile profile = createStudentInternal(req, tenant);
                results.add(BulkCreateStudentResult.ok(req.email(), req.rollNumber(), profile));
                successCount++;
            } catch (ApiException ex) {
                log.warn("Bulk student create skipped '{}': {}", req.email(), ex.getMessage());
                results.add(BulkCreateStudentResult.fail(req.email(), req.rollNumber(), ex.getMessage()));
            } catch (Exception ex) {
                log.error("Bulk student create unexpected failure for '{}': {}", req.email(), ex.getMessage(), ex);
                results.add(BulkCreateStudentResult.fail(req.email(), req.rollNumber(), "Unexpected error: " + ex.getMessage()));
            }
        }

        // Tenant-wide notification summarising the batch
        if (successCount > 0) {
            int failed = requests.size() - successCount;
            String msg = String.format(
                    "%d student account(s) created successfully%s. Welcome emails with login credentials have been sent.",
                    successCount,
                    failed > 0 ? " (" + failed + " skipped due to errors)" : "");
            eventPublisher.publishEvent(
                    NotificationEvent.of(tenant, "BULK_STUDENTS_CREATED", "Bulk Student Import", msg));
        }

        auditService.log("BULK_STUDENTS_CREATED", "StudentProfile", null,
                String.format("Bulk student create: %d/%d succeeded", successCount, requests.size()));
        return results;
    }

    // ── Read operations ──────────────────────────────────────────────────

    public List<StudentProfile> list() {
        return students.findByTenantId(TenantContext.requireTenantId());
    }

    public StudentProfile get(Long id) {
        return students.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Student not found: " + id));
    }

    /** The profile of the currently authenticated student. */
    public StudentProfile myProfile() {
        Long tenant = TenantContext.requireTenantId();
        return students.findByUserId(CurrentUser.id())
                .filter(p -> tenant.equals(p.getTenantId()))
                .orElseThrow(() -> ApiException.notFound("No student profile for the current user"));
    }

    public StudentProfile updateAcademics(Long id, UpdateAcademicsRequest req) {
        StudentProfile p = get(id);
        if (req.cgpa() != null) {
            if (req.cgpa().compareTo(BigDecimal.TEN) > 0) {
                throw ApiException.badRequest("CGPA cannot exceed 10.0");
            }
            p.setCgpa(req.cgpa());
        }
        if (req.activeBacklogs() != null) p.setActiveBacklogs(req.activeBacklogs());
        if (req.currentTerm() != null) p.setCurrentTerm(req.currentTerm());
        return students.save(p);
    }

    // ── Private helpers ──────────────────────────────────────────────────

    private StudentProfile createStudentInternal(BulkStudentRequest req, Long tenant) {
        if (users.existsByTenantIdAndEmailIgnoreCase(tenant, req.email())) {
            throw ApiException.conflict("Email already registered: " + req.email());
        }
        if (students.existsByTenantIdAndRollNumberIgnoreCase(tenant, req.rollNumber())) {
            throw ApiException.conflict("Roll number already exists: " + req.rollNumber());
        }

        String rawPassword = generatePassword(12);

        User user = new User();
        user.setTenantId(tenant);
        user.setEmail(req.email());
        user.setFullName(req.fullName());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setStatus(UserStatus.ACTIVE);
        user.setMustChangePassword(true);
        user.setRoles(Set.of(roles.findByName(RoleName.STUDENT.name())
                .orElseThrow(() -> ApiException.badRequest("STUDENT role not seeded"))));
        user = users.save(user);

        StudentProfile profile = new StudentProfile();
        profile.setTenantId(tenant);
        profile.setUserId(user.getId());
        profile.setRollNumber(req.rollNumber());
        profile.setBranch(req.branch());
        profile.setBatchYear(req.batchYear());
        profile.setProgramId(req.programId());
        profile = students.save(profile);

        // Send welcome email with portal URL, credentials, and student-specific guidance
        sendStudentWelcomeEmail(req.email(), rawPassword);

        // In-app notification for the new student
        notificationService.notify(
                user.getId(), tenant,
                "STUDENT_WELCOME",
                "Welcome to Campus360!",
                "Your student account is ready. Sign in to view your timetable, attendance, and more.",
                "StudentProfile", profile.getId());

        return profile;
    }

    private void sendStudentWelcomeEmail(String email, String rawPassword) {
        String loginUrl = portalUrl + "/login";
        String passwordHtml = "<p><strong>Password:</strong> <span style=\"font-family: monospace; font-size: 18px; background: #e2e8f0; padding: 4px 8px; border-radius: 6px; letter-spacing: 1px;\">" + rawPassword + "</span> <span style=\"color: #64748b; font-size: 14px; margin-left: 8px;\">(temporary)</span></p>";
        
        String body = com.campus360.platform.mail.EmailTemplateBuilder.build(
                "Your Student Account is Ready",
                "Welcome to Campus360!",
                """
                <p>Welcome to Campus360! Your student account has been successfully created.</p>
                <div style="background-color: #f1f5f9; padding: 20px; border-radius: 8px; margin: 24px 0;">
                    <table style="width: 100%%; text-align: left; font-family: inherit; font-size: 15px;">
                        <tr><td style="padding: 6px 0; color: #64748b; width: 30%%;">Email</td><td style="font-weight: 600;">%s</td></tr>
                    </table>
                </div>
                <div style="margin: 24px 0; padding: 16px; border: 1px dashed #cbd5e1; border-radius: 8px; background-color: #f8fafc;">
                    %s
                    <p style="font-size: 13px; color: #64748b; margin-top: 8px; margin-bottom: 0;">You will be asked to set your own secure password on your first sign-in.</p>
                </div>
                <h3 style="margin-top: 32px; font-size: 18px; color: #0f172a;">As a STUDENT, you can:</h3>
                <ul style="padding-left: 20px; color: #475569; line-height: 1.8;">
                  <li>View your timetable, attendance, and results</li>
                  <li>Check fee dues and download receipts</li>
                  <li>Explore placement opportunities and apply</li>
                  <li>Access your AI career guidance assistant</li>
                </ul>
                <p style="margin-top: 24px;">If you did not expect this email, please contact your institution administrator.</p>
                """.formatted(email, passwordHtml),
                loginUrl, "Sign In to Portal");

        mailService.send(email, "Welcome to Campus360 — Your Student Account is Ready", body);
    }

    private static String generatePassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(PWD_CHARS.charAt(RANDOM.nextInt(PWD_CHARS.length())));
        }
        return sb.toString();
    }
}

