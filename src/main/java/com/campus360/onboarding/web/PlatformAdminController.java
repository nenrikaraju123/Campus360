package com.campus360.onboarding.web;

import com.campus360.institution.domain.Institution;
import com.campus360.onboarding.domain.RegistrationStatus;
import com.campus360.onboarding.domain.TenantRegistration;
import com.campus360.onboarding.service.ProvisionResult;
import com.campus360.onboarding.service.TenantOnboardingService;
import com.campus360.onboarding.service.TenantStatsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Platform control plane — only the SUPER_ADMIN operates tenants here.
 *
 * <p>All state-changing operations fire real-time notifications to
 * both platform admins and the affected institution admin users.
 */
@RestController
@RequestMapping("/api/v1/platform")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Platform Administration", description = "Tenant approval & lifecycle (SUPER_ADMIN only)")
public class PlatformAdminController {

    private final TenantOnboardingService service;

    public PlatformAdminController(TenantOnboardingService service) {
        this.service = service;
    }

    // ── Registration review queue ─────────────────────────────────────────────

    @GetMapping("/registrations")
    @Operation(summary = "List tenant registration requests (optionally filter by status)")
    public List<TenantRegistration> registrations(@RequestParam(required = false) RegistrationStatus status) {
        return service.list(status);
    }

    @GetMapping("/registrations/{id}")
    @Operation(summary = "Get a single registration request with full detail")
    public TenantRegistration registration(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/registrations/{id}/approve")
    @Operation(summary = "Approve a request and provision the institution + first admin. Notifies the institution admin by email and in-app.")
    public ProvisionResult approve(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewDecisionRequest req) {
        return service.approve(id, req == null ? null : req.notes());
    }

    @PostMapping("/registrations/{id}/reject")
    @Operation(summary = "Reject a registration. A detailed rejection email is sent to the applicant.")
    public TenantRegistration reject(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewDecisionRequest req) {
        return service.reject(id, req == null ? null : req.notes());
    }

    // ── Direct tenant management ──────────────────────────────────────────────

    @PostMapping("/institutions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Directly provision an institution + admin (bypass the request queue)")
    public ProvisionResult createInstitution(@Valid @RequestBody CreateInstitutionRequest req) {
        return service.createInstitution(req);
    }

    @GetMapping("/institutions")
    @Operation(summary = "List all institutions on the platform")
    public List<Institution> institutions() {
        return service.listInstitutions();
    }

    @PutMapping("/institutions/{id}")
    @Operation(summary = "Update institution profile details (name, type, address)")
    public Institution updateInstitution(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInstitutionRequest req) {
        return service.updateInstitution(id, req);
    }

    @DeleteMapping("/institutions/{id}")
    @Operation(summary = "Soft-deactivate a tenant (preserves all data for compliance)")
    public Institution deactivateInstitution(@PathVariable Long id) {
        return service.deactivateInstitution(id);
    }

    @PostMapping("/institutions/{id}/suspend")
    @Operation(summary = "Suspend a tenant — its users can no longer sign in. Notifies the institution.")
    public Institution suspend(@PathVariable Long id) {
        return service.setStatus(id, "SUSPENDED");
    }

    @PostMapping("/institutions/{id}/activate")
    @Operation(summary = "Reactivate a suspended tenant. Notifies the institution.")
    public Institution activate(@PathVariable Long id) {
        return service.setStatus(id, "ACTIVE");
    }

    // ── Tenant stats ─────────────────────────────────────────────────────────

    @GetMapping("/institutions/{id}/stats")
    @Operation(summary = "Get per-tenant usage stats: student count, faculty count, total users, etc.")
    public TenantStatsDto tenantStats(@PathVariable Long id) {
        return service.getTenantStats(id);
    }

    @GetMapping("/institutions/stats")
    @Operation(summary = "Get usage stats for ALL tenants (used by the platform dashboard)")
    public List<TenantStatsDto> allTenantStats() {
        return service.getAllTenantStats();
    }
}
