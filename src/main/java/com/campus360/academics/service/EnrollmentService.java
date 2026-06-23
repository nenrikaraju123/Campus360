package com.campus360.academics.service;

import com.campus360.academics.domain.Enrollment;
import com.campus360.academics.repository.EnrollmentRepository;
import com.campus360.academics.web.dto.EnrollmentRequest;
import com.campus360.institution.domain.Section;
import com.campus360.institution.repository.SectionRepository;
import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.student.domain.StudentProfile;
import com.campus360.student.repository.StudentProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Course registration / enrollment engine. Enforces capacity limits,
 * duplicate prevention, and tenant scoping on all operations.
 */
@Service
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollments;
    private final SectionRepository sections;
    private final StudentProfileRepository students;
    private final AuditService auditService;

    public EnrollmentService(EnrollmentRepository enrollments, SectionRepository sections,
                             StudentProfileRepository students, AuditService auditService) {
        this.enrollments = enrollments;
        this.sections = sections;
        this.students = students;
        this.auditService = auditService;
    }

    /** Enroll a student in a section, checking capacity and duplicates. */
    public Enrollment enroll(EnrollmentRequest req) {
        Long tenant = TenantContext.requireTenantId();

        // Validate student exists in this tenant
        StudentProfile student = students.findByIdAndTenantId(req.studentId(), tenant)
                .orElseThrow(() -> ApiException.badRequest("Student not found: " + req.studentId()));

        // Validate section exists in this tenant
        Section section = sections.findByIdAndTenantId(req.sectionId(), tenant)
                .orElseThrow(() -> ApiException.badRequest("Section not found: " + req.sectionId()));

        // Check for duplicate enrollment
        if (enrollments.existsByStudentIdAndSectionId(req.studentId(), req.sectionId())) {
            throw ApiException.conflict("Student is already enrolled in this section");
        }

        // Check capacity
        long currentCount = enrollments.countActiveBySectionId(req.sectionId());
        if (currentCount >= section.getCapacity()) {
            // Auto-waitlist instead of rejecting
            Enrollment e = buildEnrollment(tenant, req);
            e.setStatus("WAITLISTED");
            auditService.logAsync("ENROLLMENT_WAITLISTED", "Enrollment", null,
                    "Student " + req.studentId() + " waitlisted in section " + req.sectionId());
            return enrollments.save(e);
        }

        Enrollment e = buildEnrollment(tenant, req);
        e = enrollments.save(e);
        auditService.logAsync("ENROLLMENT_CREATED", "Enrollment", e.getId(),
                "Student " + req.studentId() + " enrolled in section " + req.sectionId());
        return e;
    }

    /** Drop a student from a section. */
    public Enrollment drop(Long enrollmentId) {
        Enrollment e = getEnrollment(enrollmentId);
        if (!"ENROLLED".equals(e.getStatus()) && !"WAITLISTED".equals(e.getStatus())) {
            throw ApiException.badRequest("Cannot drop enrollment with status: " + e.getStatus());
        }
        e.setStatus("DROPPED");
        auditService.log("ENROLLMENT_DROPPED", "Enrollment", e.getId(),
                "Student " + e.getStudentId() + " dropped from section " + e.getSectionId());
        return enrollments.save(e);
    }

    @Transactional(readOnly = true)
    public Enrollment getEnrollment(Long id) {
        return enrollments.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Enrollment not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Enrollment> listByTerm(Long termId, Pageable pageable) {
        return enrollments.findByTenantIdAndTermId(TenantContext.requireTenantId(), termId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> listByStudent(Long studentId) {
        return enrollments.findByTenantIdAndStudentId(TenantContext.requireTenantId(), studentId);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> listBySection(Long sectionId) {
        return enrollments.findByTenantIdAndSectionId(TenantContext.requireTenantId(), sectionId);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> activeByStudent(Long studentId) {
        return enrollments.findActiveByStudent(TenantContext.requireTenantId(), studentId);
    }

    private Enrollment buildEnrollment(Long tenant, EnrollmentRequest req) {
        Enrollment e = new Enrollment();
        e.setTenantId(tenant);
        e.setStudentId(req.studentId());
        e.setSectionId(req.sectionId());
        e.setTermId(req.termId());
        e.setStatus("ENROLLED");
        return e;
    }
}
