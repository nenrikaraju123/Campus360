package com.campus360.academics.service;

import com.campus360.academics.domain.*;
import com.campus360.academics.repository.*;
import com.campus360.academics.web.dto.*;
import com.campus360.institution.domain.Course;
import com.campus360.institution.domain.Section;
import com.campus360.institution.repository.CourseRepository;
import com.campus360.institution.repository.SectionRepository;
import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.student.domain.StudentProfile;
import com.campus360.student.repository.StudentProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campus360.faculty.repository.FacultyCourseAssignmentRepository;
import com.campus360.faculty.repository.FacultyProfileRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Assessment, mark entry, and GPA computation engine.
 *
 * <p>Grade scale (10-point, standard Indian):
 * <pre>
 *   A+  90–100 → 10     A  80–89 → 9      B+  70–79 → 8
 *   B   60–69 → 7       C   50–59 → 6      D   40–49 → 5
 *   F   0–39  → 0       W  (withdrawn)      I  (incomplete)
 * </pre>
 */
@Service
@Transactional
public class GradeService {

    private static final Logger log = LoggerFactory.getLogger(GradeService.class);

    private static final Map<String, BigDecimal> GRADE_POINTS = Map.of(
            "A+", BigDecimal.TEN,
            "A", BigDecimal.valueOf(9),
            "B+", BigDecimal.valueOf(8),
            "B", BigDecimal.valueOf(7),
            "C", BigDecimal.valueOf(6),
            "D", BigDecimal.valueOf(5),
            "F", BigDecimal.ZERO
    );

    private final AssessmentRepository assessments;
    private final MarkRepository marks;
    private final EnrollmentRepository enrollments;
    private final TermResultRepository termResults;
    private final SectionRepository sections;
    private final CourseRepository courses;
    private final StudentProfileRepository students;
    private final AuditService auditService;
    private final FacultyProfileRepository facultyProfileRepo;
    private final FacultyCourseAssignmentRepository facultyCourseAssignRepo;

    public GradeService(AssessmentRepository assessments, MarkRepository marks,
                        EnrollmentRepository enrollments, TermResultRepository termResults,
                        SectionRepository sections, CourseRepository courses,
                        StudentProfileRepository students, AuditService auditService,
                        FacultyProfileRepository facultyProfileRepo,
                        FacultyCourseAssignmentRepository facultyCourseAssignRepo) {
        this.assessments = assessments;
        this.marks = marks;
        this.enrollments = enrollments;
        this.termResults = termResults;
        this.sections = sections;
        this.courses = courses;
        this.students = students;
        this.auditService = auditService;
        this.facultyProfileRepo = facultyProfileRepo;
        this.facultyCourseAssignRepo = facultyCourseAssignRepo;
    }

    // ---- Assessment CRUD ----
    public Assessment createAssessment(AssessmentRequest req) {
        Long tenant = TenantContext.requireTenantId();
        sections.findByIdAndTenantId(req.sectionId(), tenant)
                .orElseThrow(() -> ApiException.badRequest("Section not found: " + req.sectionId()));

        Assessment a = new Assessment();
        a.setTenantId(tenant);
        a.setSectionId(req.sectionId());
        a.setTitle(req.title());
        if (req.type() != null) a.setType(req.type().toUpperCase());
        a.setMaxMarks(req.maxMarks());
        if (req.weightagePct() != null) a.setWeightagePct(req.weightagePct());
        if (req.dueDate() != null) a.setDueDate(req.dueDate());
        a.setInstructions(req.instructions());
        return assessments.save(a);
    }

    @Transactional(readOnly = true)
    public List<Assessment> listAssessments(Long sectionId) {
        return assessments.findByTenantIdAndSectionId(TenantContext.requireTenantId(), sectionId);
    }

    @Transactional(readOnly = true)
    public Assessment getAssessment(Long id) {
        return assessments.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Assessment not found: " + id));
    }

    // ---- Mark entry (bulk) ----
    public List<Mark> enterMarks(BulkMarkRequest req) {
        Long tenant = TenantContext.requireTenantId();
        Assessment assessment = getAssessment(req.assessmentId());

        boolean isFaculty = false;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            isFaculty = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_FACULTY"));
        }

        if (CurrentUser.principal() != null && isFaculty) {
            Long userId = CurrentUser.principal().userId();
            Long facultyId = facultyProfileRepo.findByTenantIdAndUserId(tenant, userId)
                    .map(com.campus360.faculty.domain.FacultyProfile::getId)
                    .orElseThrow(() -> ApiException.badRequest("Faculty profile not found"));
            boolean assigned = facultyCourseAssignRepo.existsByTenantIdAndFacultyIdAndSectionId(
                    tenant, facultyId, assessment.getSectionId());
            if (!assigned) {
                throw ApiException.badRequest("You are not assigned to this section");
            }
        }

        String gradedBy = CurrentUser.principal() != null ? CurrentUser.principal().email() : "system";

        List<Mark> result = new ArrayList<>();
        for (MarkEntry entry : req.marks()) {
            if (entry.score().compareTo(assessment.getMaxMarks()) > 0) {
                throw ApiException.badRequest("Score " + entry.score() +
                        " exceeds max marks " + assessment.getMaxMarks() +
                        " for enrollment " + entry.enrollmentId());
            }

            Mark m = marks.findByAssessmentIdAndEnrollmentId(req.assessmentId(), entry.enrollmentId())
                    .orElse(null);
            String oldScore = null;
            if (m != null) {
                oldScore = m.getScore().toPlainString();
                m.setScore(entry.score());
                m.setRemarks(entry.remarks());
                m.setGradedBy(gradedBy);
            } else {
                m = new Mark();
                m.setTenantId(tenant);
                m.setAssessmentId(req.assessmentId());
                m.setEnrollmentId(entry.enrollmentId());
                m.setScore(entry.score());
                m.setRemarks(entry.remarks());
                m.setGradedBy(gradedBy);
            }
            result.add(marks.save(m));

            auditService.log("MARK_ENTERED", "Mark", m.getId(),
                    "Assessment=" + req.assessmentId() + " enrollment=" + entry.enrollmentId() +
                    " score=" + entry.score() + (oldScore != null ? " (was " + oldScore + ")" : ""));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<Mark> marksForAssessment(Long assessmentId) {
        getAssessment(assessmentId); // tenant validation
        return marks.findByAssessmentId(assessmentId);
    }

    // ---- GPA/CGPA computation ----

    /**
     * Computes letter grades for all enrollments in a section based on
     * weighted assessment scores, then triggers SGPA/CGPA computation.
     */
    public List<Enrollment> finalizeGrades(Long sectionId) {
        Long tenant = TenantContext.requireTenantId();
        Section section = sections.findByIdAndTenantId(sectionId, tenant)
                .orElseThrow(() -> ApiException.notFound("Section not found: " + sectionId));

        List<Assessment> sectionAssessments = assessments.findByTenantIdAndSectionId(tenant, sectionId);
        if (sectionAssessments.isEmpty()) {
            throw ApiException.badRequest("No assessments defined for this section");
        }

        List<Enrollment> sectionEnrollments = enrollments.findByTenantIdAndSectionId(tenant, sectionId);
        List<Enrollment> graded = new ArrayList<>();

        for (Enrollment enrollment : sectionEnrollments) {
            if (!"ENROLLED".equals(enrollment.getStatus())) continue;

            double weightedTotal = 0;
            double weightedMax = 0;

            for (Assessment assess : sectionAssessments) {
                Mark mark = marks.findByAssessmentIdAndEnrollmentId(assess.getId(), enrollment.getId())
                        .orElse(null);
                double weight = assess.getWeightagePct().doubleValue();
                if (mark != null) {
                    double pct = mark.getScore().doubleValue() / assess.getMaxMarks().doubleValue();
                    weightedTotal += pct * weight;
                }
                weightedMax += weight;
            }

            double finalPct = weightedMax == 0 ? 0 : (weightedTotal / weightedMax) * 100;
            String grade = computeLetterGrade(finalPct);
            BigDecimal gp = GRADE_POINTS.getOrDefault(grade, BigDecimal.ZERO);

            enrollment.setGrade(grade);
            enrollment.setGradePoints(gp);
            enrollment.setStatus("COMPLETED");
            graded.add(enrollments.save(enrollment));

            auditService.log("GRADE_FINALIZED", "Enrollment", enrollment.getId(),
                    "Student " + enrollment.getStudentId() + " grade=" + grade + " gp=" + gp);
        }

        // Trigger SGPA/CGPA recomputation for all affected students
        graded.stream().map(Enrollment::getStudentId).distinct()
                .forEach(sid -> computeGPA(sid, section.getTermId()));

        return graded;
    }

    /**
     * Computes SGPA for a specific term and CGPA across all terms.
     * Updates both the TermResult and the StudentProfile.
     */
    public TermResult computeGPA(Long studentId, Long termId) {
        Long tenant = TenantContext.requireTenantId();
        StudentProfile student = students.findByIdAndTenantId(studentId, tenant)
                .orElseThrow(() -> ApiException.notFound("Student not found: " + studentId));

        // SGPA for this term
        List<Enrollment> termEnrollments = enrollments.findByTenantIdAndStudentIdAndTermId(tenant, studentId, termId)
                .stream().filter(e -> "COMPLETED".equals(e.getStatus()) && e.getGradePoints() != null)
                .toList();

        int creditsAttempted = 0;
        int creditsEarned = 0;
        BigDecimal qualityPoints = BigDecimal.ZERO;

        for (Enrollment e : termEnrollments) {
            int credits = getCreditHours(e.getSectionId());
            creditsAttempted += credits;
            if (e.getGradePoints().compareTo(BigDecimal.ZERO) > 0) {
                creditsEarned += credits;
            }
            qualityPoints = qualityPoints.add(e.getGradePoints().multiply(BigDecimal.valueOf(credits)));
        }

        BigDecimal sgpa = creditsAttempted == 0 ? BigDecimal.ZERO
                : qualityPoints.divide(BigDecimal.valueOf(creditsAttempted), 2, RoundingMode.HALF_UP);

        // CGPA across all terms
        List<TermResult> allResults = termResults.findByTenantIdAndStudentIdOrderByTermIdAsc(tenant, studentId);
        BigDecimal totalQP = qualityPoints;
        int totalCredits = creditsAttempted;
        for (TermResult tr : allResults) {
            if (!tr.getTermId().equals(termId)) {
                totalQP = totalQP.add(tr.getSgpa().multiply(BigDecimal.valueOf(tr.getCreditsAttempted())));
                totalCredits += tr.getCreditsAttempted();
            }
        }
        BigDecimal cgpa = totalCredits == 0 ? BigDecimal.ZERO
                : totalQP.divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP);

        // Upsert TermResult
        TermResult tr = termResults.findByStudentIdAndTermId(studentId, termId).orElse(new TermResult());
        tr.setTenantId(tenant);
        tr.setStudentId(studentId);
        tr.setTermId(termId);
        tr.setCreditsAttempted(creditsAttempted);
        tr.setCreditsEarned(creditsEarned);
        tr.setSgpa(sgpa);
        tr.setCgpa(cgpa);
        tr = termResults.save(tr);

        // Update StudentProfile CGPA
        student.setCgpa(cgpa);
        students.save(student);

        log.info("GPA computed: student={} term={} SGPA={} CGPA={}", studentId, termId, sgpa, cgpa);
        return tr;
    }

    @Transactional(readOnly = true)
    public List<TermResult> transcript(Long studentId) {
        Long tenant = TenantContext.requireTenantId();
        students.findByIdAndTenantId(studentId, tenant)
                .orElseThrow(() -> ApiException.notFound("Student not found: " + studentId));
        return termResults.findByTenantIdAndStudentIdOrderByTermIdAsc(tenant, studentId);
    }

    // ---- Helpers ----
    private String computeLetterGrade(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B+";
        if (percentage >= 60) return "B";
        if (percentage >= 50) return "C";
        if (percentage >= 40) return "D";
        return "F";
    }

    private int getCreditHours(Long sectionId) {
        return sections.findById(sectionId)
                .flatMap(s -> courses.findByIdAndTenantId(s.getCourseId(), s.getTenantId()))
                .map(Course::getCreditHours)
                .orElse(3); // default 3 credits
    }
}
