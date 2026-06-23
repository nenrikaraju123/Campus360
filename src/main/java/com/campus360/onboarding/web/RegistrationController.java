package com.campus360.onboarding.web;

import com.campus360.onboarding.domain.TenantRegistration;
import com.campus360.onboarding.service.TenantOnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Public endpoint for institutions to apply for onboarding. */
@RestController
@RequestMapping("/api/v1/registrations")
@Tag(name = "Tenant Registration", description = "Public self-registration requests")
public class RegistrationController {

    private final TenantOnboardingService service;

    public RegistrationController(TenantOnboardingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Submit a request to onboard a new institution (pending platform review)")
    public Map<String, Object> submit(@Valid @RequestBody RegisterTenantRequest req) {
        TenantRegistration r = service.submit(req);
        return Map.of(
                "id", r.getId(),
                "status", r.getStatus(),
                "message", "Your registration is pending review by the platform administrator.");
    }
}
