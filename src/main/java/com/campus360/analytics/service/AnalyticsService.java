package com.campus360.analytics.service;

import com.campus360.academics.repository.*;
import com.campus360.analytics.web.dto.*;
import com.campus360.finance.repository.InvoiceRepository;
import com.campus360.institution.repository.*;
import com.campus360.placement.repository.*;
import com.campus360.student.domain.StudentProfile;
import com.campus360.student.repository.StudentProfileRepository;
import com.campus360.studentlife.repository.GrievanceRepository;
import com.campus360.platform.tenancy.TenantContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise analytics & reporting service. Provides dashboard summaries
 * per role and at-risk student detection using multiple signals.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final StudentProfileRepository students;
    private final DepartmentRepository departments;
    private final ProgramRepository programs;
    private final CourseRepository courses;
    private final SectionRepository sections;
    private final EnrollmentRepository enrollments;
    private final AttendanceRepository attendance;
    private final JobPostingRepository postings;
    private final ApplicationRepository applications;
    private final OfferRepository offers;
    private final InvoiceRepository invoices;
    private final GrievanceRepository grievances;

    public AnalyticsService(StudentProfileRepository students, DepartmentRepository departments,
                            ProgramRepository programs, CourseRepository courses,
                            SectionRepository sections, EnrollmentRepository enrollments,
                            AttendanceRepository attendance, JobPostingRepository postings,
                            ApplicationRepository applications, OfferRepository offers,
                            InvoiceRepository invoices, GrievanceRepository grievances) {
        this.students = students;
        this.departments = departments;
        this.programs = programs;
        this.courses = courses;
        this.sections = sections;
        this.enrollments = enrollments;
        this.attendance = attendance;
        this.postings = postings;
        this.applications = applications;
        this.offers = offers;
        this.invoices = invoices;
        this.grievances = grievances;
    }

    /** High-level institutional dashboard for INSTITUTION_ADMIN. */
    public InstitutionDashboard institutionDashboard() {
        Long tenant = TenantContext.requireTenantId();
        long studentCount = students.findByTenantId(tenant).size();
        long deptCount = departments.findByTenantId(tenant).size();
        long programCount = programs.findByTenantId(tenant).size();
        long courseCount = courses.findByTenantId(tenant).size();
        long sectionCount = sections.findByTenantId(tenant).size();
        long openPostings = postings.findByTenantIdAndStatus(tenant, "OPEN").size();
        long totalOffers = offers.findByTenantId(tenant).size();
        long pendingInvoices = invoices.findByTenantIdAndStatus(tenant, "PENDING", PageRequest.of(0, 1)).getTotalElements();
        long openGrievances = grievances.findByTenantIdAndStatusOrderByCreatedAtDesc(tenant, "OPEN", PageRequest.of(0, 1)).getTotalElements();

        return new InstitutionDashboard(studentCount, deptCount, programCount, courseCount,
                sectionCount, openPostings, totalOffers, pendingInvoices, openGrievances);
    }

    /**
     * At-risk student detection. A student is flagged at-risk if any of:
     * <ul>
     *   <li>CGPA below 5.0</li>
     *   <li>Active backlogs ≥ 2</li>
     *   <li>Attendance below 75% in any enrolled course (if attendance data available)</li>
     *   <li>Outstanding fee balance (overdue invoices)</li>
     * </ul>
     */
    public List<AtRiskStudent> atRiskStudents() {
        Long tenant = TenantContext.requireTenantId();
        List<StudentProfile> allStudents = students.findByTenantId(tenant);
        List<AtRiskStudent> atRisk = new ArrayList<>();

        for (StudentProfile s : allStudents) {
            List<String> reasons = new ArrayList<>();

            if (s.getCgpa().compareTo(BigDecimal.valueOf(5.0)) < 0) {
                reasons.add("Low CGPA: " + s.getCgpa());
            }
            if (s.getActiveBacklogs() >= 2) {
                reasons.add("Active backlogs: " + s.getActiveBacklogs());
            }

            // Check attendance on active enrollments
            var activeEnrollments = enrollments.findActiveByStudent(tenant, s.getId());
            for (var enr : activeEnrollments) {
                long total = attendance.countTotalByEnrollment(enr.getId());
                if (total >= 5) { // only check if enough data
                    long attended = attendance.countAttendedByEnrollment(enr.getId());
                    double pct = (attended * 100.0) / total;
                    if (pct < 75.0) {
                        reasons.add("Low attendance (" + String.format("%.1f%%", pct)
                                + ") in section " + enr.getSectionId());
                    }
                }
            }

            // Check overdue invoices
            var overdueInvs = invoices.findByTenantIdAndStudentIdAndStatus(tenant, s.getId(), "PENDING");
            long overdue = overdueInvs.stream()
                    .filter(i -> i.getDueDate() != null && i.getDueDate().isBefore(java.time.LocalDate.now()))
                    .count();
            if (overdue > 0) {
                reasons.add("Overdue invoices: " + overdue);
            }

            if (!reasons.isEmpty()) {
                atRisk.add(new AtRiskStudent(s.getId(), s.getRollNumber(), s.getBranch(),
                        s.getCgpa(), s.getActiveBacklogs(), reasons));
            }
        }
        return atRisk;
    }
}
