package com.campus360.admissions.web;

import com.campus360.admissions.domain.AdmissionLead;
import com.campus360.admissions.repository.AdmissionLeadRepository;
import com.campus360.admissions.web.dto.AdmissionLeadRequest;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.tenancy.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/admissions/leads")
@Tag(name = "Admissions", description = "Admission leads/enquiries")
public class AdmissionLeadController {

    private final AdmissionLeadRepository repository;

    public AdmissionLeadController(AdmissionLeadRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN', 'HOD', 'FACULTY')")
    @Operation(summary = "List leads for the current tenant")
    public Page<AdmissionLead> list(@RequestParam(required = false) String status, Pageable pageable) {
        Long tenantId = TenantContext.requireTenantId();
        if (status != null && !status.isBlank()) {
            return repository.findByTenantIdAndStatus(tenantId, status, pageable);
        }
        return repository.findByTenantId(tenantId, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN', 'HOD', 'FACULTY')")
    public AdmissionLead get(@PathVariable Long id) {
        return repository.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Lead not found: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public AdmissionLead create(@Valid @RequestBody AdmissionLeadRequest req) {
        Long tenantId = TenantContext.requireTenantId();
        AdmissionLead lead = new AdmissionLead();
        lead.setTenantId(tenantId);
        lead.setFirstName(req.firstName);
        lead.setLastName(req.lastName);
        lead.setEmail(req.email);
        lead.setPhone(req.phone);
        lead.setSource(req.source);
        lead.setProgramInterest(req.programInterest);
        lead.setNotes(req.notes);
        lead.setAssignedTo(req.assignedTo);
        lead.setCreatedBy(CurrentUser.principal() != null ? CurrentUser.principal().userId() : null);
        return repository.save(lead);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'SUPER_ADMIN')")
    public AdmissionLead update(@PathVariable Long id, @Valid @RequestBody AdmissionLeadRequest req) {
        Long tenantId = TenantContext.requireTenantId();
        AdmissionLead lead = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> ApiException.notFound("Lead not found: " + id));
        lead.setFirstName(req.firstName);
        lead.setLastName(req.lastName);
        lead.setEmail(req.email);
        lead.setPhone(req.phone);
        lead.setSource(req.source);
        lead.setProgramInterest(req.programInterest);
        lead.setNotes(req.notes);
        lead.setAssignedTo(req.assignedTo);
        lead.setUpdatedAt(Instant.now());
        return repository.save(lead);
    }
}
