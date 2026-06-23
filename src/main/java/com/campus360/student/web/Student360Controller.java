package com.campus360.student.web;

import com.campus360.platform.error.ApiException;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.student.domain.StudentGuardian;
import com.campus360.student.domain.StudentLifecycleHistory;
import com.campus360.student.domain.StudentProfile;
import com.campus360.student.repository.StudentGuardianRepository;
import com.campus360.student.repository.StudentLifecycleHistoryRepository;
import com.campus360.student.repository.StudentProfileRepository;
import com.campus360.student.service.StudentLifecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "Students", description = "Student 360 lifecycle management")
public class Student360Controller {

    private final StudentProfileRepository profileRepository;
    private final StudentGuardianRepository guardianRepository;
    private final StudentLifecycleHistoryRepository historyRepository;
    private final StudentLifecycleService lifecycleService;

    public Student360Controller(StudentProfileRepository profileRepository,
                                StudentGuardianRepository guardianRepository,
                                StudentLifecycleHistoryRepository historyRepository,
                                StudentLifecycleService lifecycleService) {
        this.profileRepository = profileRepository;
        this.guardianRepository = guardianRepository;
        this.historyRepository = historyRepository;
        this.lifecycleService = lifecycleService;
    }

    @GetMapping("/{id}/profile-360")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN', 'HOD', 'FACULTY', 'STUDENT')")
    @Operation(summary = "Get full student 360 profile")
    public StudentProfile get360(@PathVariable Long id) {
        Long tenantId = TenantContext.requireTenantId();
        return profileRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> ApiException.notFound("Student not found: " + id));
    }

    @PutMapping("/{id}/personal")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public StudentProfile updatePersonal(@PathVariable Long id,
                                         @RequestBody Map<String, Object> body) {
        Long tenantId = TenantContext.requireTenantId();
        StudentProfile student = profileRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> ApiException.notFound("Student not found: " + id));

        if (body.containsKey("gender")) student.setGender((String) body.get("gender"));
        if (body.containsKey("nationality")) student.setNationality((String) body.get("nationality"));
        if (body.containsKey("bloodGroup")) student.setBloodGroup((String) body.get("bloodGroup"));
        if (body.containsKey("emergencyContactName")) student.setEmergencyContactName((String) body.get("emergencyContactName"));
        if (body.containsKey("emergencyContactPhone")) student.setEmergencyContactPhone((String) body.get("emergencyContactPhone"));
        if (body.containsKey("category")) student.setCategory((String) body.get("category"));
        if (body.containsKey("quota")) student.setQuota((String) body.get("quota"));

        return profileRepository.save(student);
    }

    @PutMapping("/{id}/academic")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public StudentProfile updateAcademic(@PathVariable Long id,
                                          @RequestBody Map<String, Object> body) {
        Long tenantId = TenantContext.requireTenantId();
        StudentProfile student = profileRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> ApiException.notFound("Student not found: " + id));

        if (body.containsKey("branch")) student.setBranch((String) body.get("branch"));
        if (body.containsKey("batchYear")) student.setBatchYear((Integer) body.get("batchYear"));
        if (body.containsKey("currentAcademicStanding")) student.setCurrentAcademicStanding((String) body.get("currentAcademicStanding"));

        return profileRepository.save(student);
    }

    // ---- Guardians ----

    @GetMapping("/{id}/guardians")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN', 'HOD', 'FACULTY')")
    public List<StudentGuardian> listGuardians(@PathVariable Long id) {
        TenantContext.requireTenantId();
        return guardianRepository.findByStudentIdOrderByIsPrimaryDesc(id);
    }

    @PostMapping("/{id}/guardians")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public StudentGuardian addGuardian(@PathVariable Long id,
                                       @RequestBody StudentGuardian req) {
        Long tenantId = TenantContext.requireTenantId();
        profileRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> ApiException.notFound("Student not found: " + id));
        req.setTenantId(tenantId);
        req.setStudentId(id);
        return guardianRepository.save(req);
    }

    @PutMapping("/{id}/guardians/{guardianId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public StudentGuardian updateGuardian(@PathVariable Long id,
                                          @PathVariable Long guardianId,
                                          @RequestBody StudentGuardian req) {
        Long tenantId = TenantContext.requireTenantId();
        StudentGuardian guardian = guardianRepository.findByIdAndTenantId(guardianId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Guardian not found: " + guardianId));
        guardian.setFullName(req.getFullName());
        guardian.setRelationship(req.getRelationship());
        guardian.setEmail(req.getEmail());
        guardian.setPhone(req.getPhone());
        guardian.setOccupation(req.getOccupation());
        guardian.setAnnualIncome(req.getAnnualIncome());
        guardian.setPrimary(req.isPrimary());
        guardian.setUpdatedAt(Instant.now());
        return guardianRepository.save(guardian);
    }

    // ---- Lifecycle History ----

    @GetMapping("/{id}/lifecycle-history")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN', 'HOD')")
    public List<StudentLifecycleHistory> lifecycleHistory(@PathVariable Long id) {
        return lifecycleService.getHistory(id);
    }

    // ---- Lifecycle Actions ----

    @PostMapping("/{id}/actions/promote")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public StudentProfile promote(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String comment = body != null ? body.get("comment") : null;
        return lifecycleService.promote(id, comment);
    }

    @PostMapping("/{id}/actions/suspend")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public StudentProfile suspend(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String comment = body != null ? body.get("comment") : null;
        return lifecycleService.transition(id, "SUSPENDED", "SUSPENDED", comment);
    }

    @PostMapping("/{id}/actions/graduate")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public StudentProfile graduate(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String comment = body != null ? body.get("comment") : null;
        return lifecycleService.transition(id, "GRADUATED", "GRADUATED", comment);
    }

    @PostMapping("/{id}/actions/archive")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public StudentProfile archive(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String comment = body != null ? body.get("comment") : null;
        return lifecycleService.transition(id, "ARCHIVED", "ARCHIVED", comment);
    }

    @PostMapping("/{id}/actions/transfer-section")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public StudentProfile transferSection(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long tenantId = TenantContext.requireTenantId();
        StudentProfile student = profileRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> ApiException.notFound("Student not found: " + id));
        // Section transfer just updates the program for now; timetable module will hook in later
        String comment = (String) body.getOrDefault("comment", "Section transfer");
        return lifecycleService.transition(id, "TRANSFERRED", "ACTIVE", comment);
    }

    // ---- Documents ----

    @GetMapping("/{id}/documents")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN', 'HOD', 'FACULTY')")
    public List<Object> listDocuments(@PathVariable Long id) {
        // Placeholder for StudentDocumentRepository call
        return List.of();
    }

    @PostMapping("/{id}/documents")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public Object addDocument(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        // Placeholder for creating StudentDocument
        return req;
    }

    // ---- Bulk Import ----

    @PostMapping("/bulk-import/template")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public String downloadTemplate() {
        return "Template URL or CSV content";
    }

    @PostMapping("/bulk-import")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public Map<String, String> startBulkImport(@RequestBody List<Map<String, Object>> rows) {
        // Returns jobId
        return Map.of("jobId", "job-" + System.currentTimeMillis());
    }

    @GetMapping("/bulk-import/{jobId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public Map<String, Object> getImportStatus(@PathVariable String jobId) {
        return Map.of("status", "READY", "validRows", 10, "invalidRows", 0);
    }

    @PostMapping("/bulk-import/{jobId}/actions/commit")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public Map<String, String> commitImport(@PathVariable String jobId) {
        return Map.of("status", "COMPLETED");
    }
}
