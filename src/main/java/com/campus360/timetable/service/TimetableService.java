package com.campus360.timetable.service;

import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.timetable.domain.TimetableEntry;
import com.campus360.timetable.domain.TimetableTemplate;
import com.campus360.timetable.repository.TimetableEntryRepository;
import com.campus360.timetable.repository.TimetableTemplateRepository;
import com.campus360.timetable.web.dto.CreateTimetableEntryRequest;
import com.campus360.timetable.web.dto.CreateTimetableTemplateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class TimetableService {

    private final TimetableTemplateRepository templateRepo;
    private final TimetableEntryRepository entryRepo;
    private final TimetableConflictService conflictService;
    private final AuditService auditService;

    public TimetableService(TimetableTemplateRepository templateRepo,
                            TimetableEntryRepository entryRepo,
                            TimetableConflictService conflictService,
                            AuditService auditService) {
        this.templateRepo = templateRepo;
        this.entryRepo = entryRepo;
        this.conflictService = conflictService;
        this.auditService = auditService;
    }

    // ---- Templates ----
    public TimetableTemplate createTemplate(CreateTimetableTemplateRequest req) {
        Long tenantId = TenantContext.requireTenantId();

        TimetableTemplate t = new TimetableTemplate();
        t.setTenantId(tenantId);
        t.setName(req.name());
        t.setTermId(req.termId());
        t.setAcademicYear(req.academicYear());
        t = templateRepo.save(t);

        auditService.log("TIMETABLE_TEMPLATE_CREATED", "TimetableTemplate", t.getId(), "Created template: " + req.name());
        return t;
    }

    public TimetableTemplate publishTemplate(Long id) {
        Long tenantId = TenantContext.requireTenantId();
        TimetableTemplate t = templateRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> ApiException.notFound("Template not found"));

        if (!"DRAFT".equals(t.getStatus())) {
            throw ApiException.badRequest("Only DRAFT templates can be published");
        }

        conflictService.detectConflicts(id);
        if (conflictService.hasUnresolvedConflicts(id)) {
            throw ApiException.badRequest("Cannot publish timetable with unresolved conflicts");
        }

        t.setStatus("PUBLISHED");
        t.setPublishedAt(Instant.now());
        if (CurrentUser.principal() != null) {
            t.setPublishedBy(CurrentUser.principal().userId());
        }
        t = templateRepo.save(t);

        auditService.log("TIMETABLE_PUBLISHED", "TimetableTemplate", id, "Published timetable template");
        return t;
    }

    @Transactional(readOnly = true)
    public List<TimetableTemplate> listTemplates(Long termId) {
        Long tenantId = TenantContext.requireTenantId();
        if (termId != null) {
            return templateRepo.findByTenantIdAndTermId(tenantId, termId);
        }
        return templateRepo.findAll(); // Simplified; ideally paginated
    }

    // ---- Entries ----
    public TimetableEntry addEntry(Long templateId, CreateTimetableEntryRequest req) {
        Long tenantId = TenantContext.requireTenantId();
        TimetableTemplate t = templateRepo.findByIdAndTenantId(templateId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Template not found"));

        if (!"DRAFT".equals(t.getStatus())) {
            throw ApiException.badRequest("Can only add entries to DRAFT templates");
        }

        TimetableEntry e = new TimetableEntry();
        e.setTenantId(tenantId);
        e.setTemplateId(templateId);
        e.setSectionId(req.sectionId());
        e.setCourseId(req.courseId());
        e.setFacultyId(req.facultyId());
        e.setRoomId(req.roomId());
        e.setTimeSlotId(req.timeSlotId());
        e.setDayOfWeek(req.dayOfWeek().toUpperCase());
        if (req.entryType() != null) e.setEntryType(req.entryType().toUpperCase());
        e = entryRepo.save(e);

        // Auto-trigger conflict detection when entries change
        conflictService.detectConflicts(templateId);

        auditService.log("TIMETABLE_ENTRY_ADDED", "TimetableEntry", e.getId(), "Added entry to template " + templateId);
        return e;
    }

    public void removeEntry(Long templateId, Long entryId) {
        Long tenantId = TenantContext.requireTenantId();
        TimetableEntry e = entryRepo.findByIdAndTenantId(entryId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Entry not found"));

        if (!e.getTemplateId().equals(templateId)) {
            throw ApiException.badRequest("Entry does not belong to this template");
        }

        entryRepo.delete(e);
        conflictService.detectConflicts(templateId);
        auditService.log("TIMETABLE_ENTRY_REMOVED", "TimetableEntry", entryId, "Removed entry from template " + templateId);
    }

    @Transactional(readOnly = true)
    public List<TimetableEntry> getEntriesBySection(Long templateId, Long sectionId) {
        Long tenantId = TenantContext.requireTenantId();
        return entryRepo.findByTenantIdAndTemplateIdAndSectionId(tenantId, templateId, sectionId);
    }
}
