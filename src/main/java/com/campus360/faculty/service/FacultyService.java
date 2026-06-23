package com.campus360.faculty.service;

import com.campus360.faculty.domain.*;
import com.campus360.faculty.repository.*;
import com.campus360.faculty.web.dto.*;
import com.campus360.iam.domain.User;
import com.campus360.iam.service.UserManagementService;
import com.campus360.iam.web.dto.CreateUserRequest;
import com.campus360.institution.repository.DepartmentRepository;
import com.campus360.institution.repository.SectionRepository;
import com.campus360.institution.repository.CourseRepository;
import com.campus360.notification.domain.NotificationEvent;
import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.numbering.NumberingService;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.shared.dto.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Enterprise faculty management: profile creation with IAM user provisioning,
 * course assignments, department linking, and employee code generation.
 */
@Service
@Transactional
public class FacultyService {

    private static final Logger log = LoggerFactory.getLogger(FacultyService.class);

    private final FacultyProfileRepository profileRepo;
    private final FacultyDepartmentAssignmentRepository deptAssignRepo;
    private final FacultyCourseAssignmentRepository courseAssignRepo;
    private final DepartmentRepository departments;
    private final SectionRepository sections;
    private final CourseRepository courses;
    private final UserManagementService userService;
    private final NumberingService numberingService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    public FacultyService(FacultyProfileRepository profileRepo,
                          FacultyDepartmentAssignmentRepository deptAssignRepo,
                          FacultyCourseAssignmentRepository courseAssignRepo,
                          DepartmentRepository departments,
                          SectionRepository sections,
                          CourseRepository courses,
                          UserManagementService userService,
                          NumberingService numberingService,
                          AuditService auditService,
                          ApplicationEventPublisher eventPublisher) {
        this.profileRepo = profileRepo;
        this.deptAssignRepo = deptAssignRepo;
        this.courseAssignRepo = courseAssignRepo;
        this.departments = departments;
        this.sections = sections;
        this.courses = courses;
        this.userService = userService;
        this.numberingService = numberingService;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    // ---- Create faculty ----
    public FacultyProfile create(CreateFacultyRequest req) {
        Long tenantId = TenantContext.requireTenantId();

        // Duplicate check
        if (profileRepo.findByTenantIdAndEmail(tenantId, req.email()).isPresent()) {
            throw ApiException.conflict("Faculty email already registered: " + req.email());
        }

        // Validate department if provided
        if (req.departmentId() != null) {
            departments.findByIdAndTenantId(req.departmentId(), tenantId)
                    .orElseThrow(() -> ApiException.badRequest("Department not found: " + req.departmentId()));
        }

        // 1. Create IAM user with requested roles (default to FACULTY if empty)
        String fullName = req.firstName() + (req.lastName() != null ? " " + req.lastName() : "");
        List<String> roles = (req.roles() != null && !req.roles().isEmpty()) ? req.roles() : List.of("FACULTY");
        User iamUser = userService.createUser(new CreateUserRequest(
                fullName, req.email(), null, roles));

        // 2. Generate employee code via NumberingService
        String employeeCode = numberingService.generateNextNumber("FACULTY", tenantId.toString());

        // 3. Build faculty profile
        FacultyProfile profile = new FacultyProfile();
        profile.setTenantId(tenantId);
        profile.setUserId(iamUser.getId());
        profile.setEmployeeCode(employeeCode);
        profile.setFirstName(req.firstName());
        profile.setLastName(req.lastName());
        profile.setEmail(req.email());
        profile.setPhone(req.phone());
        profile.setDepartmentId(req.departmentId());
        profile.setDesignation(req.designation());
        profile.setQualification(req.qualification());
        if (req.employmentType() != null) profile.setEmploymentType(req.employmentType().toUpperCase());
        profile.setJoiningDate(req.joiningDate());
        profile = profileRepo.save(profile);

        // 4. Create primary department assignment if provided
        if (req.departmentId() != null) {
            FacultyDepartmentAssignment fda = new FacultyDepartmentAssignment();
            fda.setTenantId(tenantId);
            fda.setFacultyId(profile.getId());
            fda.setDepartmentId(req.departmentId());
            fda.setIsPrimary(true);
            deptAssignRepo.save(fda);
        }

        auditService.log("FACULTY_CREATED", "FacultyProfile", profile.getId(),
                "Created faculty " + employeeCode + " (" + req.email() + ")");

        return profile;
    }

    // ---- Bulk Create ----
    public List<BulkCreateFacultyResult> bulkCreateFaculty(List<BulkFacultyRequest> requests) {
        Long tenantId = TenantContext.requireTenantId();
        List<BulkCreateFacultyResult> results = new java.util.ArrayList<>(requests.size());
        int successCount = 0;

        for (BulkFacultyRequest req : requests) {
            try {
                CreateFacultyRequest createReq = new CreateFacultyRequest(
                        req.firstName(), req.lastName(), req.email(), req.phone(),
                        req.departmentId(), req.designation(), req.qualification(),
                        req.employmentType(), req.joiningDate(), req.roles()
                );
                FacultyProfile profile = create(createReq);
                results.add(BulkCreateFacultyResult.ok(req.email(), profile));
                successCount++;
            } catch (ApiException ex) {
                log.warn("Bulk faculty create skipped '{}': {}", req.email(), ex.getMessage());
                results.add(BulkCreateFacultyResult.fail(req.email(), ex.getMessage()));
            } catch (Exception ex) {
                log.error("Bulk faculty create unexpected failure for '{}': {}", req.email(), ex.getMessage(), ex);
                results.add(BulkCreateFacultyResult.fail(req.email(), "Unexpected error: " + ex.getMessage()));
            }
        }

        if (successCount > 0) {
            int failed = requests.size() - successCount;
            String msg = String.format(
                    "%d faculty account(s) created successfully%s. Welcome emails with login credentials have been sent.",
                    successCount,
                    failed > 0 ? " (" + failed + " skipped due to errors)" : "");
            eventPublisher.publishEvent(
                    NotificationEvent.of(tenantId, "BULK_FACULTY_CREATED", "Bulk Faculty Import", msg));
        }

        auditService.log("BULK_FACULTY_CREATED", "FacultyProfile", null,
                String.format("Bulk faculty create: %d/%d succeeded", successCount, requests.size()));
        return results;
    }

    // ---- Read ----
    @Transactional(readOnly = true)
    public FacultyProfile get(Long id) {
        Long tenantId = TenantContext.requireTenantId();
        return profileRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> ApiException.notFound("Faculty not found: " + id));
    }

    @Transactional(readOnly = true)
    public FacultyProfile getByUserId(Long userId) {
        Long tenantId = TenantContext.requireTenantId();
        return profileRepo.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> ApiException.notFound("Faculty profile not found for user: " + userId));
    }

    @Transactional(readOnly = true)
    public PageResponse<FacultyProfile> list(int page, int size) {
        Long tenantId = TenantContext.requireTenantId();
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return PageResponse.of(profileRepo.findByTenantId(tenantId, pageable));
    }

    // ---- Update ----
    public FacultyProfile update(Long id, CreateFacultyRequest req) {
        FacultyProfile profile = get(id);
        if (req.firstName() != null) profile.setFirstName(req.firstName());
        if (req.lastName() != null) profile.setLastName(req.lastName());
        if (req.phone() != null) profile.setPhone(req.phone());
        if (req.departmentId() != null) {
            departments.findByIdAndTenantId(req.departmentId(), profile.getTenantId())
                    .orElseThrow(() -> ApiException.badRequest("Department not found: " + req.departmentId()));
            profile.setDepartmentId(req.departmentId());
        }
        if (req.designation() != null) profile.setDesignation(req.designation());
        if (req.qualification() != null) profile.setQualification(req.qualification());
        if (req.employmentType() != null) profile.setEmploymentType(req.employmentType().toUpperCase());
        if (req.joiningDate() != null) profile.setJoiningDate(req.joiningDate());
        profile.setUpdatedAt(Instant.now());

        auditService.log("FACULTY_UPDATED", "FacultyProfile", id,
                "Updated faculty " + profile.getEmployeeCode());
        return profileRepo.save(profile);
    }

    // ---- Course assignments ----
    public FacultyCourseAssignment assignCourse(Long facultyId, FacultyCourseAssignmentRequest req) {
        Long tenantId = TenantContext.requireTenantId();
        FacultyProfile faculty = get(facultyId);

        sections.findByIdAndTenantId(req.sectionId(), tenantId)
                .orElseThrow(() -> ApiException.badRequest("Section not found: " + req.sectionId()));
        courses.findByIdAndTenantId(req.courseId(), tenantId)
                .orElseThrow(() -> ApiException.badRequest("Course not found: " + req.courseId()));

        // Duplicate check
        if (courseAssignRepo.findByTenantIdAndFacultyIdAndSectionIdAndCourseId(
                tenantId, facultyId, req.sectionId(), req.courseId()).isPresent()) {
            throw ApiException.conflict("Faculty already assigned to this course/section");
        }

        FacultyCourseAssignment fca = new FacultyCourseAssignment();
        fca.setTenantId(tenantId);
        fca.setFacultyId(facultyId);
        fca.setSectionId(req.sectionId());
        fca.setCourseId(req.courseId());
        fca.setTermId(req.termId());
        fca.setAcademicYear(req.academicYear());
        fca = courseAssignRepo.save(fca);

        auditService.log("FACULTY_COURSE_ASSIGNED", "FacultyCourseAssignment", fca.getId(),
                "Faculty " + faculty.getEmployeeCode() + " assigned to section=" + req.sectionId() +
                        " course=" + req.courseId());
        return fca;
    }

    @Transactional(readOnly = true)
    public List<FacultyCourseAssignment> listCourseAssignments(Long facultyId) {
        Long tenantId = TenantContext.requireTenantId();
        return courseAssignRepo.findByTenantIdAndFacultyIdAndStatus(tenantId, facultyId, "ACTIVE");
    }
}
