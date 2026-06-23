package com.campus360.timetable.web;

import com.campus360.timetable.domain.*;
import com.campus360.timetable.service.*;
import com.campus360.timetable.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/timetable")
public class TimetableController {

    private final TimetableService timetableService;
    private final TimetableConflictService conflictService;
    private final RoomService roomService;
    private final TimeSlotService timeSlotService;

    public TimetableController(TimetableService timetableService,
                               TimetableConflictService conflictService,
                               RoomService roomService,
                               TimeSlotService timeSlotService) {
        this.timetableService = timetableService;
        this.conflictService = conflictService;
        this.roomService = roomService;
        this.timeSlotService = timeSlotService;
    }

    // ---- Rooms & Time Slots ----
    @GetMapping("/rooms")
    @PreAuthorize("isAuthenticated()")
    public List<Room> listRooms() {
        return roomService.listActive();
    }

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN')")
    public Room createRoom(@Valid @RequestBody CreateRoomRequest req) {
        return roomService.create(req);
    }

    @GetMapping("/time-slots")
    @PreAuthorize("isAuthenticated()")
    public List<TimeSlot> listTimeSlots() {
        return timeSlotService.listAll();
    }

    @PostMapping("/time-slots")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN')")
    public TimeSlot createTimeSlot(@Valid @RequestBody CreateTimeSlotRequest req) {
        return timeSlotService.create(req);
    }

    // ---- Timetable Templates ----
    @PostMapping("/templates")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public TimetableTemplate createTemplate(@Valid @RequestBody CreateTimetableTemplateRequest req) {
        return timetableService.createTemplate(req);
    }

    @GetMapping("/templates")
    @PreAuthorize("isAuthenticated()")
    public List<TimetableTemplate> listTemplates(@RequestParam(required = false) Long termId) {
        return timetableService.listTemplates(termId);
    }

    @PostMapping("/templates/{id}/publish")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public TimetableTemplate publishTemplate(@PathVariable Long id) {
        return timetableService.publishTemplate(id);
    }

    @GetMapping("/templates/{id}/conflicts")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public List<TimetableConflict> getConflicts(@PathVariable Long id) {
        return conflictService.getConflicts(id);
    }

    // ---- Timetable Entries ----
    @PostMapping("/templates/{id}/entries")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public TimetableEntry addEntry(@PathVariable Long id, @Valid @RequestBody CreateTimetableEntryRequest req) {
        return timetableService.addEntry(id, req);
    }

    @DeleteMapping("/templates/{id}/entries/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'HOD')")
    public void removeEntry(@PathVariable Long id, @PathVariable Long entryId) {
        timetableService.removeEntry(id, entryId);
    }

    @GetMapping("/templates/{id}/entries")
    @PreAuthorize("isAuthenticated()")
    public List<TimetableEntry> getEntriesForSection(@PathVariable Long id, @RequestParam Long sectionId) {
        return timetableService.getEntriesBySection(id, sectionId);
    }
}
