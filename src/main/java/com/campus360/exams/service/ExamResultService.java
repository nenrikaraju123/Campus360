package com.campus360.exams.service;

import com.campus360.exams.domain.*;
import com.campus360.exams.repository.*;
import com.campus360.exams.web.dto.ResultPublicationRequest;
import com.campus360.institution.domain.Course;
import com.campus360.institution.repository.CourseRepository;
import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.student.domain.StudentProfile;
import com.campus360.student.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExamResultService {

    private static final Map<String, BigDecimal> GRADE_POINTS = Map.of(
            "A+", BigDecimal.TEN,
            "A", BigDecimal.valueOf(9),
            "B+", BigDecimal.valueOf(8),
            "B", BigDecimal.valueOf(7),
            "C", BigDecimal.valueOf(6),
            "D", BigDecimal.valueOf(5),
            "F", BigDecimal.ZERO
    );

    private final ExamCycleRepository cycleRepo;
    private final ExamComponentRepository compRepo;
    private final ExamMarkSheetRepository markSheetRepo;
    private final ExamMarkRepository markRepo;
    private final GradeCardRepository gradeCardRepo;
    private final ResultPublicationRepository pubRepo;
    private final CourseRepository courseRepo;
    private final StudentProfileRepository studentRepo;
    private final AuditService auditService;

    public ExamResultService(ExamCycleRepository cycleRepo,
                             ExamComponentRepository compRepo,
                             ExamMarkSheetRepository markSheetRepo,
                             ExamMarkRepository markRepo,
                             GradeCardRepository gradeCardRepo,
                             ResultPublicationRepository pubRepo,
                             CourseRepository courseRepo,
                             StudentProfileRepository studentRepo,
                             AuditService auditService) {
        this.cycleRepo = cycleRepo;
        this.compRepo = compRepo;
        this.markSheetRepo = markSheetRepo;
        this.markRepo = markRepo;
        this.gradeCardRepo = gradeCardRepo;
        this.pubRepo = pubRepo;
        this.courseRepo = courseRepo;
        this.studentRepo = studentRepo;
        this.auditService = auditService;
    }

    /**
     * Generates or updates GradeCards for all students in a published mark sheet.
     */
    public void generateGradeCardsForMarkSheet(Long markSheetId) {
        Long tenantId = TenantContext.requireTenantId();
        ExamMarkSheet ms = markSheetRepo.findByIdAndTenantId(markSheetId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Mark sheet not found"));

        if (!"PUBLISHED".equals(ms.getStatus())) {
            throw ApiException.badRequest("Can only generate grade cards from PUBLISHED mark sheets");
        }

        List<ExamMark> marks = markRepo.findByTenantIdAndMarkSheetId(tenantId, markSheetId);
        if (marks.isEmpty()) return;

        Map<Long, List<ExamMark>> studentMarks = marks.stream().collect(Collectors.groupingBy(ExamMark::getStudentId));

        for (Map.Entry<Long, List<ExamMark>> entry : studentMarks.entrySet()) {
            Long studentId = entry.getKey();
            generateGradeCard(tenantId, studentId, ms.getExamCycleId());
        }
    }

    private void generateGradeCard(Long tenantId, Long studentId, Long examCycleId) {
        // Find all published mark sheets for this student in this cycle
        List<ExamMarkSheet> publishedSheets = markSheetRepo.findByTenantIdAndExamCycleIdAndStatus(tenantId, examCycleId, "PUBLISHED");
        if (publishedSheets.isEmpty()) return;

        List<Long> sheetIds = publishedSheets.stream().map(ExamMarkSheet::getId).toList();

        // Compute total GPA
        int totalCredits = 0;
        int earnedCredits = 0;
        BigDecimal qualityPoints = BigDecimal.ZERO;
        boolean hasFail = false;

        Map<Long, Course> courseCache = new HashMap<>();

        for (ExamMarkSheet ms : publishedSheets) {
            List<ExamMark> marks = markRepo.findByTenantIdAndMarkSheetId(tenantId, ms.getId()).stream()
                    .filter(m -> m.getStudentId().equals(studentId))
                    .toList();
            if (marks.isEmpty()) continue;

            Course course = courseCache.computeIfAbsent(ms.getCourseId(), cid ->
                    courseRepo.findByIdAndTenantId(cid, tenantId).orElseThrow());

            List<ExamComponent> components = compRepo.findByTenantIdAndExamCycleIdAndCourseId(tenantId, examCycleId, course.getId());
            double weightedTotal = 0;
            double weightedMax = 0;

            for (ExamComponent comp : components) {
                ExamMark mark = marks.stream().filter(m -> m.getExamComponentId().equals(comp.getId())).findFirst().orElse(null);
                double weight = comp.getWeightagePct().doubleValue();

                if (mark != null && !mark.getIsAbsent() && mark.getMarksObtained() != null) {
                    double pct = mark.getMarksObtained().doubleValue() / comp.getMaxMarks().doubleValue();
                    weightedTotal += pct * weight;
                }
                weightedMax += weight;
            }

            double finalPct = weightedMax == 0 ? 0 : (weightedTotal / weightedMax) * 100;
            String grade = computeLetterGrade(finalPct);
            BigDecimal gp = GRADE_POINTS.getOrDefault(grade, BigDecimal.ZERO);

            int credits = course.getCreditHours();
            totalCredits += credits;
            if (gp.compareTo(BigDecimal.ZERO) > 0) {
                earnedCredits += credits;
            } else {
                hasFail = true;
            }
            qualityPoints = qualityPoints.add(gp.multiply(BigDecimal.valueOf(credits)));
        }

        BigDecimal sgpa = totalCredits == 0 ? BigDecimal.ZERO
                : qualityPoints.divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP);

        GradeCard gc = gradeCardRepo.findByTenantIdAndStudentIdAndExamCycleId(tenantId, studentId, examCycleId)
                .orElse(new GradeCard());

        gc.setTenantId(tenantId);
        gc.setStudentId(studentId);
        gc.setExamCycleId(examCycleId);
        gc.setTotalCredits(BigDecimal.valueOf(totalCredits));
        gc.setEarnedCredits(BigDecimal.valueOf(earnedCredits));
        gc.setSgpa(sgpa);

        // CGPA calculation (simplified for this phase, just copy SGPA or compute cumulative if needed)
        // A full CGPA recalculation would read all previous GradeCards
        gc.setCgpa(sgpa);
        gc.setResultStatus(hasFail ? "FAIL" : "PASS");

        gradeCardRepo.save(gc);
        auditService.log("GRADE_CARD_GENERATED", "GradeCard", gc.getId(), "Generated GC for student " + studentId);
    }

    public ResultPublication publishResults(Long examCycleId, ResultPublicationRequest req) {
        Long tenantId = TenantContext.requireTenantId();

        ExamCycle cycle = cycleRepo.findByIdAndTenantId(examCycleId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Exam cycle not found"));

        if (pubRepo.findByTenantIdAndExamCycleIdAndProgramId(tenantId, examCycleId, req.programId()).isPresent()) {
            throw ApiException.conflict("Results already published for this program in this cycle");
        }

        // Publish all grade cards for students in this program (simplified logic)
        List<StudentProfile> students = studentRepo.findByTenantIdAndProgramId(tenantId, req.programId());
        for (StudentProfile s : students) {
            gradeCardRepo.findByTenantIdAndStudentIdAndExamCycleId(tenantId, s.getId(), examCycleId)
                    .ifPresent(gc -> {
                        gc.setIsPublished(true);
                        gc.setPublishedAt(Instant.now());
                        gradeCardRepo.save(gc);
                    });
        }

        ResultPublication pub = new ResultPublication();
        pub.setTenantId(tenantId);
        pub.setExamCycleId(examCycleId);
        pub.setProgramId(req.programId());
        pub.setTermId(req.termId());
        pub.setPublishedAt(Instant.now());
        pub.setPublishedBy(CurrentUser.principal() != null ? CurrentUser.principal().userId() : 0L);
        pub.setRemarks(req.remarks());
        pub = pubRepo.save(pub);

        cycle.setStatus("RESULTS_DECLARED");
        cycleRepo.save(cycle);

        auditService.log("RESULTS_PUBLISHED", "ResultPublication", pub.getId(), "Published results for program " + req.programId());
        return pub;
    }

    private String computeLetterGrade(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B+";
        if (percentage >= 60) return "B";
        if (percentage >= 50) return "C";
        if (percentage >= 40) return "D";
        return "F";
    }
}
