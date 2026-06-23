package com.campus360.admissions.service;

import com.campus360.admissions.domain.AdmissionApplication;
import com.campus360.admissions.repository.AdmissionApplicationRepository;
import com.campus360.iam.domain.Role;
import com.campus360.iam.domain.User;
import com.campus360.iam.domain.UserStatus;
import com.campus360.iam.repository.RoleRepository;
import com.campus360.iam.repository.UserRepository;
import com.campus360.notification.repository.OutboxMessageRepository;
import com.campus360.notification.domain.OutboxMessage;
import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.numbering.NumberingService;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.student.domain.StudentProfile;
import com.campus360.student.repository.StudentProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Handles admission → enrollment conversion.
 * Creates the IAM user, student profile, assigns student number,
 * queues welcome email via Outbox, and writes audit log.
 *
 * Enrollment is never blocked by email failure — email is queued
 * in the same transaction using the Outbox pattern.
 */
@Service
@Transactional
public class AdmissionConversionService {

    private final AdmissionApplicationRepository applicationRepository;
    private final AdmissionWorkflowService workflowService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentProfileRepository studentRepository;
    private final NumberingService numberingService;
    private final OutboxMessageRepository outboxRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AdmissionConversionService(
            AdmissionApplicationRepository applicationRepository,
            AdmissionWorkflowService workflowService,
            UserRepository userRepository,
            RoleRepository roleRepository,
            StudentProfileRepository studentRepository,
            NumberingService numberingService,
            OutboxMessageRepository outboxRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {
        this.applicationRepository = applicationRepository;
        this.workflowService = workflowService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.studentRepository = studentRepository;
        this.numberingService = numberingService;
        this.outboxRepository = outboxRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public StudentProfile enroll(Long applicationId, String comment) {
        Long tenantId = TenantContext.requireTenantId();
        AdmissionApplication app = applicationRepository.findByIdAndTenantId(applicationId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Application not found: " + applicationId));

        if (!"OFFERED".equals(app.getStatus())) {
            throw ApiException.badRequest("Application must be in OFFERED status to enroll. Current: " + app.getStatus());
        }

        if (userRepository.existsByTenantIdAndEmailIgnoreCase(tenantId, app.getEmail())) {
            throw ApiException.conflict("A user account already exists for email: " + app.getEmail());
        }

        // 1. Create IAM User with temporary password
        String tempPassword = UUID.randomUUID().toString().substring(0, 12);
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> ApiException.serverError("STUDENT role not configured"));

        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail(app.getEmail());
        user.setFullName(app.getFirstName() + " " + app.getLastName());
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setStatus(UserStatus.ACTIVE);
        user.setMustChangePassword(true);
        user.getRoles().add(studentRole);
        user = userRepository.save(user);

        // 2. Generate student number
        String studentNumber = numberingService.generateNextNumber("STUDENT", app.getAcademicYear());
        String admissionNumber = app.getApplicationNumber();

        // 3. Create Student Profile
        StudentProfile profile = new StudentProfile();
        profile.setTenantId(tenantId);
        profile.setUserId(user.getId());
        profile.setProgramId(app.getProgramId());
        profile.setRollNumber(studentNumber);
        profile.setAdmissionDate(LocalDate.now());
        profile.setCurrentTerm(1);
        profile.setEnrollmentDate(LocalDate.now());
        profile.setLifecycleStatus("ACTIVE");
        profile.setCategory(app.getCategory());
        profile.setQuota(app.getQuota());
        profile.setGender(app.getGender());
        profile.setDateOfBirth(app.getDateOfBirth());
        profile.setAdmissionNumber(admissionNumber);
        profile = studentRepository.save(profile);

        // 4. Transition application status → ENROLLED
        workflowService.transition(applicationId, "ENROLLED", comment);

        // 5. Audit log
        auditService.log("STUDENT_ENROLLED", "StudentProfile", profile.getId(),
                String.format("From application %d. User: %s, Student#: %s",
                        applicationId, app.getEmail(), studentNumber));

        // 6. Queue welcome email via Outbox (never blocks enrollment)
        queueWelcomeEmail(tenantId, app, studentNumber, tempPassword);

        return profile;
    }

    private void queueWelcomeEmail(Long tenantId, AdmissionApplication app,
                                   String studentNumber, String tempPassword) {
        String subject = "Welcome to Campus360 — Your Student Account is Ready";
        String body = String.format(
                "Dear %s %s,\n\n" +
                "Welcome! Your student account has been created.\n\n" +
                "Student Number: %s\n" +
                "Login Email:    %s\n" +
                "Temp Password:  %s\n\n" +
                "Please log in and change your password immediately.\n\n" +
                "Best regards,\nCampus360",
                app.getFirstName(), app.getLastName(),
                studentNumber, app.getEmail(), tempPassword);

        OutboxMessage msg = new OutboxMessage();
        msg.setTenantId(tenantId);
        msg.setType("EMAIL");
        msg.setRecipient(app.getEmail());
        msg.setSubject(subject);
        msg.setPayload(body);
        outboxRepository.save(msg);
    }
}
