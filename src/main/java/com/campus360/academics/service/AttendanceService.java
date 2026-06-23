package com.campus360.academics.service;

import com.campus360.academics.domain.AttendanceRecord;
import com.campus360.academics.domain.ClassMeeting;
import com.campus360.academics.domain.Enrollment;
import com.campus360.academics.repository.AttendanceRepository;
import com.campus360.academics.repository.ClassMeetingRepository;
import com.campus360.academics.repository.EnrollmentRepository;
import com.campus360.academics.web.dto.*;
import com.campus360.notification.domain.NotificationEvent;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.tenancy.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Attendance management service. Supports single and bulk marking,
 * attendance percentage computation, and low-attendance auto-alerts.
 */
@Service
@Transactional
public class AttendanceService {

    private static final double LOW_ATTENDANCE_THRESHOLD = 75.0;
    private static final Set<String> VALID_STATUSES = Set.of("PRESENT", "ABSENT", "LATE", "EXCUSED");

    private final ClassMeetingRepository meetings;
    private final AttendanceRepository attendance;
    private final EnrollmentRepository enrollments;
    private final ApplicationEventPublisher events;

    public AttendanceService(ClassMeetingRepository meetings, AttendanceRepository attendance,
                             EnrollmentRepository enrollments, ApplicationEventPublisher events) {
        this.meetings = meetings;
        this.attendance = attendance;
        this.enrollments = enrollments;
        this.events = events;
    }

    // ---- Class meetings ----
    public ClassMeeting createMeeting(ClassMeetingRequest req) {
        Long tenant = TenantContext.requireTenantId();
        ClassMeeting m = new ClassMeeting();
        m.setTenantId(tenant);
        m.setSectionId(req.sectionId());
        m.setMeetingDate(req.meetingDate());
        m.setTopic(req.topic());
        m.setCreatedBy(CurrentUser.principal() != null ? CurrentUser.principal().email() : "system");
        return meetings.save(m);
    }

    @Transactional(readOnly = true)
    public List<ClassMeeting> listMeetings(Long sectionId) {
        return meetings.findBySectionIdOrderByMeetingDateDesc(sectionId);
    }

    // ---- Bulk attendance marking ----
    public List<AttendanceRecord> markBulk(BulkAttendanceRequest req) {
        Long tenant = TenantContext.requireTenantId();
        ClassMeeting meeting = meetings.findByIdAndTenantId(req.meetingId(), tenant)
                .orElseThrow(() -> ApiException.badRequest("Meeting not found: " + req.meetingId()));

        String source = req.source() != null ? req.source().toUpperCase() : "MANUAL";
        String markedBy = CurrentUser.principal() != null ? CurrentUser.principal().email() : "system";

        List<AttendanceRecord> records = new ArrayList<>();
        for (AttendanceEntry entry : req.records()) {
            String status = entry.status().toUpperCase();
            if (!VALID_STATUSES.contains(status)) {
                throw ApiException.badRequest("Invalid attendance status: " + entry.status());
            }

            // Skip duplicates silently
            if (attendance.existsByEnrollmentIdAndMeetingId(entry.enrollmentId(), req.meetingId())) {
                continue;
            }

            AttendanceRecord ar = new AttendanceRecord();
            ar.setTenantId(tenant);
            ar.setEnrollmentId(entry.enrollmentId());
            ar.setMeetingId(req.meetingId());
            ar.setStatus(status);
            ar.setSource(source);
            ar.setMarkedBy(markedBy);
            records.add(attendance.save(ar));
        }

        // Check for low attendance and publish alerts
        checkLowAttendance(tenant, req.records());

        return records;
    }

    // ---- Attendance summary ----
    @Transactional(readOnly = true)
    public AttendanceSummary summary(Long enrollmentId) {
        Enrollment e = enrollments.findByIdAndTenantId(enrollmentId, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Enrollment not found: " + enrollmentId));

        long total = attendance.countTotalByEnrollment(enrollmentId);
        long attended = attendance.countAttendedByEnrollment(enrollmentId);
        long absent = total - attended;
        double pct = total == 0 ? 100.0 : Math.round((attended * 10000.0) / total) / 100.0;

        return new AttendanceSummary(enrollmentId, e.getStudentId(), e.getSectionId(),
                total, attended, absent, pct);
    }

    @Transactional(readOnly = true)
    public List<AttendanceSummary> sectionSummary(Long sectionId) {
        Long tenant = TenantContext.requireTenantId();
        List<Enrollment> sectionEnrollments = enrollments.findByTenantIdAndSectionId(tenant, sectionId);
        return sectionEnrollments.stream().map(e -> {
            long total = attendance.countTotalByEnrollment(e.getId());
            long attended = attendance.countAttendedByEnrollment(e.getId());
            double pct = total == 0 ? 100.0 : Math.round((attended * 10000.0) / total) / 100.0;
            return new AttendanceSummary(e.getId(), e.getStudentId(), e.getSectionId(),
                    total, attended, total - attended, pct);
        }).toList();
    }

    // ---- Low attendance detection ----
    private void checkLowAttendance(Long tenant, List<AttendanceEntry> entries) {
        for (AttendanceEntry entry : entries) {
            long total = attendance.countTotalByEnrollment(entry.enrollmentId());
            if (total < 5) continue; // too early to alert

            long attended = attendance.countAttendedByEnrollment(entry.enrollmentId());
            double pct = (attended * 100.0) / total;
            if (pct < LOW_ATTENDANCE_THRESHOLD) {
                events.publishEvent(NotificationEvent.of(tenant, "LOW_ATTENDANCE",
                        "Low attendance alert",
                        "Enrollment #" + entry.enrollmentId() + " has " + 
                        String.format("%.1f%%", pct) + " attendance (below " + 
                        LOW_ATTENDANCE_THRESHOLD + "% threshold)."));
            }
        }
    }
}
