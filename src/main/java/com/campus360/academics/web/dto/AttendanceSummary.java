package com.campus360.academics.web.dto;

/** Attendance summary for a student-section enrollment. */
public record AttendanceSummary(
        Long enrollmentId,
        Long studentId,
        Long sectionId,
        long totalMeetings,
        long attended,
        long absent,
        double attendancePercent) {
}
