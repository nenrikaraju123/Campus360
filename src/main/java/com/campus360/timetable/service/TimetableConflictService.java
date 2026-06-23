package com.campus360.timetable.service;

import com.campus360.platform.audit.AuditService;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.timetable.domain.TimetableConflict;
import com.campus360.timetable.domain.TimetableEntry;
import com.campus360.timetable.repository.TimetableConflictRepository;
import com.campus360.timetable.repository.TimetableEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TimetableConflictService {

    private final TimetableConflictRepository conflictRepo;
    private final TimetableEntryRepository entryRepo;
    private final AuditService auditService;

    public TimetableConflictService(TimetableConflictRepository conflictRepo,
                                    TimetableEntryRepository entryRepo,
                                    AuditService auditService) {
        this.conflictRepo = conflictRepo;
        this.entryRepo = entryRepo;
        this.auditService = auditService;
    }

    /**
     * Re-detects all conflicts for a given template.
     * Deletes existing conflicts first.
     */
    public void detectConflicts(Long templateId) {
        Long tenantId = TenantContext.requireTenantId();
        conflictRepo.deleteByTenantIdAndTemplateId(tenantId, templateId);

        List<TimetableEntry> entries = entryRepo.findByTenantIdAndTemplateId(tenantId, templateId);

        for (int i = 0; i < entries.size(); i++) {
            for (int j = i + 1; j < entries.size(); j++) {
                TimetableEntry a = entries.get(i);
                TimetableEntry b = entries.get(j);

                // Check for overlapping time slot on the same day
                if (a.getDayOfWeek().equals(b.getDayOfWeek()) && a.getTimeSlotId().equals(b.getTimeSlotId())) {
                    checkConflict(tenantId, templateId, a, b);
                }
            }
        }
    }

    private void checkConflict(Long tenantId, Long templateId, TimetableEntry a, TimetableEntry b) {
        // Faculty Double Booked
        if (a.getFacultyId() != null && a.getFacultyId().equals(b.getFacultyId())) {
            createConflict(tenantId, templateId, a, b, "FACULTY_DOUBLE_BOOKED",
                    "Faculty is assigned to two overlapping entries");
        }
        // Room Double Booked
        if (a.getRoomId() != null && a.getRoomId().equals(b.getRoomId())) {
            createConflict(tenantId, templateId, a, b, "ROOM_DOUBLE_BOOKED",
                    "Room is assigned to two overlapping entries");
        }
        // Section Double Booked
        if (a.getSectionId() != null && a.getSectionId().equals(b.getSectionId())) {
            createConflict(tenantId, templateId, a, b, "SECTION_DOUBLE_BOOKED",
                    "Section is assigned to two overlapping entries");
        }
    }

    private void createConflict(Long tenantId, Long templateId, TimetableEntry a, TimetableEntry b,
                                String type, String desc) {
        TimetableConflict c = new TimetableConflict();
        c.setTenantId(tenantId);
        c.setTemplateId(templateId);
        c.setEntryAId(a.getId());
        c.setEntryBId(b.getId());
        c.setConflictType(type);
        c.setDescription(desc);
        conflictRepo.save(c);
        auditService.log("TIMETABLE_CONFLICT_DETECTED", "TimetableTemplate", templateId, "Conflict: " + type);
    }

    public boolean hasUnresolvedConflicts(Long templateId) {
        Long tenantId = TenantContext.requireTenantId();
        return conflictRepo.countByTenantIdAndTemplateIdAndResolvedFalse(tenantId, templateId) > 0;
    }

    @Transactional(readOnly = true)
    public List<TimetableConflict> getConflicts(Long templateId) {
        Long tenantId = TenantContext.requireTenantId();
        return conflictRepo.findByTenantIdAndTemplateId(tenantId, templateId);
    }
}
