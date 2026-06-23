package com.campus360.academics.web;

import com.campus360.academics.domain.AcademicCalendarEvent;
import com.campus360.academics.domain.AcademicHoliday;
import com.campus360.academics.service.AcademicCalendarService;
import com.campus360.academics.web.dto.CreateCalendarEventRequest;
import com.campus360.academics.web.dto.CreateHolidayRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/academics/calendar")
public class AcademicCalendarController {

    private final AcademicCalendarService service;

    public AcademicCalendarController(AcademicCalendarService service) {
        this.service = service;
    }

    // ---- Events ----
    @GetMapping("/events")
    @PreAuthorize("isAuthenticated()")
    public List<AcademicCalendarEvent> listEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return service.listEvents(start, end);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public AcademicCalendarEvent createEvent(@Valid @RequestBody CreateCalendarEventRequest req) {
        return service.createEvent(req);
    }

    @PutMapping("/events/{id}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public AcademicCalendarEvent updateEvent(@PathVariable Long id, @Valid @RequestBody CreateCalendarEventRequest req) {
        return service.updateEvent(id, req);
    }

    @DeleteMapping("/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public void deleteEvent(@PathVariable Long id) {
        service.deleteEvent(id);
    }

    // ---- Holidays ----
    @GetMapping("/holidays")
    @PreAuthorize("isAuthenticated()")
    public List<AcademicHoliday> listHolidays(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return service.listHolidays(start, end);
    }

    @PostMapping("/holidays")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public AcademicHoliday createHoliday(@Valid @RequestBody CreateHolidayRequest req) {
        return service.createHoliday(req);
    }
}
