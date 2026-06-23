package com.campus360.academics.web;

import com.campus360.academics.domain.AttendanceRecord;
import com.campus360.academics.domain.ClassMeeting;
import com.campus360.academics.service.AttendanceService;
import com.campus360.academics.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "Attendance", description = "Class meetings, bulk attendance marking, summaries")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    // ---- Meetings ----
    @PostMapping("/meetings")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a class meeting session")
    public ClassMeeting createMeeting(@Valid @RequestBody ClassMeetingRequest req) {
        return service.createMeeting(req);
    }

    @GetMapping("/meetings/by-section/{sectionId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    public List<ClassMeeting> meetings(@PathVariable Long sectionId) {
        return service.listMeetings(sectionId);
    }

    // ---- Marking ----
    @PostMapping("/mark")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Bulk-mark attendance for a class meeting (skips duplicates)")
    public List<AttendanceRecord> markBulk(@Valid @RequestBody BulkAttendanceRequest req) {
        return service.markBulk(req);
    }

    // ---- Summaries ----
    @GetMapping("/summary/{enrollmentId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY','STUDENT')")
    @Operation(summary = "Attendance percentage for a single enrollment")
    public AttendanceSummary summary(@PathVariable Long enrollmentId) {
        return service.summary(enrollmentId);
    }

    @GetMapping("/summary/by-section/{sectionId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    @Operation(summary = "Attendance summary for all students in a section")
    public List<AttendanceSummary> sectionSummary(@PathVariable Long sectionId) {
        return service.sectionSummary(sectionId);
    }
}
