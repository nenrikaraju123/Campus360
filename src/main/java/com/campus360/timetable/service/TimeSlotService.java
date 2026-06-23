package com.campus360.timetable.service;

import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.timetable.domain.TimeSlot;
import com.campus360.timetable.repository.TimeSlotRepository;
import com.campus360.timetable.web.dto.CreateTimeSlotRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepo;
    private final AuditService auditService;

    public TimeSlotService(TimeSlotRepository timeSlotRepo, AuditService auditService) {
        this.timeSlotRepo = timeSlotRepo;
        this.auditService = auditService;
    }

    public TimeSlot create(CreateTimeSlotRequest req) {
        Long tenantId = TenantContext.requireTenantId();

        if (req.endTime().isBefore(req.startTime()) || req.endTime().equals(req.startTime())) {
            throw ApiException.badRequest("End time must be after start time");
        }

        if (timeSlotRepo.findByTenantIdAndDayOfWeekAndStartTimeAndEndTime(
                tenantId, req.dayOfWeek().toUpperCase(), req.startTime(), req.endTime()).isPresent()) {
            throw ApiException.conflict("Time slot already exists for this day and time");
        }

        TimeSlot slot = new TimeSlot();
        slot.setTenantId(tenantId);
        slot.setDayOfWeek(req.dayOfWeek().toUpperCase());
        slot.setStartTime(req.startTime());
        slot.setEndTime(req.endTime());
        slot.setSlotLabel(req.slotLabel());
        if (req.isBreak() != null) slot.setIsBreak(req.isBreak());
        if (req.displayOrder() != null) slot.setDisplayOrder(req.displayOrder());
        slot = timeSlotRepo.save(slot);

        auditService.log("TIME_SLOT_CREATED", "TimeSlot", slot.getId(), "Created slot: " + req.dayOfWeek() + " " + req.startTime());
        return slot;
    }

    @Transactional(readOnly = true)
    public List<TimeSlot> listAll() {
        Long tenantId = TenantContext.requireTenantId();
        return timeSlotRepo.findByTenantIdOrderByDisplayOrderAsc(tenantId);
    }
}
