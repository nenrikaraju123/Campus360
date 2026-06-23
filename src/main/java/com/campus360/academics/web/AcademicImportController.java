package com.campus360.academics.web;

import com.campus360.importer.domain.ImportJob;
import com.campus360.importer.domain.ImportTemplate;
import com.campus360.importer.service.ImportJobService;
import com.campus360.importer.service.ImportTemplateService;
import com.campus360.platform.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/academics")
@RequiredArgsConstructor
public class AcademicImportController {

    private final ImportTemplateService templateService;
    private final ImportJobService jobService;

    @GetMapping("/import-templates/{type}/download")
    @PreAuthorize("hasAuthority('INSTITUTION_ADMIN')")
    public ImportTemplate getTemplate(@PathVariable String type) {
        return templateService.getTemplate(TenantContext.requireTenantId(), type);
    }

    @PostMapping("/imports")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('INSTITUTION_ADMIN')")
    public ImportJob createImportJob(@RequestParam("file") MultipartFile file, @RequestParam("type") String type) {
        String actorId = "SYSTEM_USER"; // Extract from Context
        // The service would save the file using document storage and create the job record
        return new ImportJob(); // Placeholder for actual job creation logic
    }

    @GetMapping("/imports/{jobId}")
    @PreAuthorize("hasAuthority('INSTITUTION_ADMIN')")
    public ImportJob getImportJob(@PathVariable Long jobId) {
        // Return job details
        return new ImportJob();
    }

    @PostMapping("/imports/{jobId}/actions/validate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('INSTITUTION_ADMIN')")
    public void validateImport(@PathVariable Long jobId) {
        // Trigger async validation
    }

    @PostMapping("/imports/{jobId}/actions/commit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('INSTITUTION_ADMIN')")
    public void commitImport(@PathVariable Long jobId) {
        // Trigger async commit
    }
}
