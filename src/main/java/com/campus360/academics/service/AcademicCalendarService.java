package com.campus360.academics.service;

import com.campus360.academics.domain.AcademicCalendarEvent;
import com.campus360.academics.domain.AcademicHoliday;
import com.campus360.academics.repository.AcademicCalendarEventRepository;
import com.campus360.academics.repository.AcademicHolidayRepository;
import com.campus360.academics.web.dto.CreateCalendarEventRequest;
import com.campus360.academics.web.dto.CreateHolidayRequest;
import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.tenancy.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AcademicCalendarService {

    private final AcademicCalendarEventRepository eventRepo;
    private final AcademicHolidayRepository holidayRepo;
    private final AuditService auditService;

    public AcademicCalendarService(AcademicCalendarEventRepository eventRepo,
                                   AcademicHolidayRepository holidayRepo,
                                   AuditService auditService) {
        this.eventRepo = eventRepo;
        this.holidayRepo = holidayRepo;
        this.auditService = auditService;
    }

    // ---- Events ----
    public AcademicCalendarEvent createEvent(CreateCalendarEventRequest req) {
        Long tenantId = TenantContext.requireTenantId();

        if (req.endDate() != null && req.endDate().isBefore(req.startDate())) {
            throw ApiException.badRequest("End date cannot be before start date");
        }

        AcademicCalendarEvent event = new AcademicCalendarEvent();
        event.setTenantId(tenantId);
        event.setTitle(req.title());
        event.setDescription(req.description());
        event.setEventType(req.eventType().toUpperCase());
        event.setStartDate(req.startDate());
        event.setEndDate(req.endDate());
        if (req.scope() != null) event.setScope(req.scope().toUpperCase());
        event.setScopeId(req.scopeId());
        if (req.isAllDay() != null) event.setIsAllDay(req.isAllDay());
        event.setCreatedBy(CurrentUser.principal() != null ? CurrentUser.principal().email() : "system");

        event = eventRepo.save(event);
        auditService.log("CALENDAR_EVENT_CREATED", "AcademicCalendarEvent", event.getId(), "Created event: " + req.title());
        return event;
    }

    public AcademicCalendarEvent updateEvent(Long id, CreateCalendarEventRequest req) {
        Long tenantId = TenantContext.requireTenantId();
        AcademicCalendarEvent event = eventRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> ApiException.notFound("Event not found"));

        event.setTitle(req.title());
        event.setDescription(req.description());
        event.setEventType(req.eventType().toUpperCase());
        event.setStartDate(req.startDate());
        event.setEndDate(req.endDate());
        if (req.scope() != null) event.setScope(req.scope().toUpperCase());
        event.setScopeId(req.scopeId());
        if (req.isAllDay() != null) event.setIsAllDay(req.isAllDay());
        event.setUpdatedAt(Instant.now());

        auditService.log("CALENDAR_EVENT_UPDATED", "AcademicCalendarEvent", event.getId(), "Updated event: " + req.title());
        return eventRepo.save(event);
    }

    public void deleteEvent(Long id) {
        Long tenantId = TenantContext.requireTenantId();
        AcademicCalendarEvent event = eventRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> ApiException.notFound("Event not found"));
        eventRepo.delete(event);
        auditService.log("CALENDAR_EVENT_DELETED", "AcademicCalendarEvent", id, "Deleted event: " + event.getTitle());
    }

    @Transactional(readOnly = true)
    public List<AcademicCalendarEvent> listEvents(LocalDate start, LocalDate end) {
        Long tenantId = TenantContext.requireTenantId();
        if (start != null && end != null) {
            return eventRepo.findEventsInDateRange(tenantId, start, end);
        }
        return eventRepo.findByTenantIdOrderByStartDateAsc(tenantId);
    }

    // ---- Holidays ----
    public AcademicHoliday createHoliday(CreateHolidayRequest req) {
        Long tenantId = TenantContext.requireTenantId();
        AcademicHoliday holiday = new AcademicHoliday();
        holiday.setTenantId(tenantId);
        holiday.setName(req.name());
        holiday.setHolidayDate(req.holidayDate());
        if (req.holidayType() != null) holiday.setHolidayType(req.holidayType().toUpperCase());
        if (req.isOptional() != null) holiday.setIsOptional(req.isOptional());

        holiday = holidayRepo.save(holiday);
        auditService.log("HOLIDAY_CREATED", "AcademicHoliday", holiday.getId(), "Created holiday: " + req.name());
        return holiday;
    }

    @Transactional(readOnly = true)
    public List<AcademicHoliday> listHolidays(LocalDate start, LocalDate end) {
        Long tenantId = TenantContext.requireTenantId();
        if (start != null && end != null) {
            return holidayRepo.findByTenantIdAndHolidayDateBetweenOrderByHolidayDateAsc(tenantId, start, end);
        }
        return holidayRepo.findByTenantIdOrderByHolidayDateAsc(tenantId);
    }
}
