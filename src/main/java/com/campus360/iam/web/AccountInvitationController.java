package com.campus360.iam.web;

import com.campus360.iam.service.AccountInvitationService;
import com.campus360.iam.service.WelcomeNotificationService;
import com.campus360.platform.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/iam")
@RequiredArgsConstructor
public class AccountInvitationController {

    private final AccountInvitationService invitationService;
    private final WelcomeNotificationService welcomeService;

    @PreAuthorize("hasAuthority('PLATFORM_ADMIN') or hasAuthority('INSTITUTION_ADMIN')")
    @PostMapping("/invitations/{id}/actions/resend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendInvitation(@PathVariable Long id) {
        String actorId = "SYSTEM_USER"; // In real scenario, extract from SecurityContextHolder
        invitationService.resendInvitation(TenantContext.requireTenantId(), id, actorId);
    }

    @PreAuthorize("hasAuthority('PLATFORM_ADMIN') or hasAuthority('INSTITUTION_ADMIN')")
    @PostMapping("/invitations/{id}/actions/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeInvitation(@PathVariable Long id) {
        String actorId = "SYSTEM_USER";
        invitationService.revokeInvitation(TenantContext.requireTenantId(), id, actorId);
    }
    
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN') or hasAuthority('INSTITUTION_ADMIN')")
    @PostMapping("/users/{userId}/actions/resend-welcome")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendWelcome(@PathVariable Long userId) {
        String actorId = "SYSTEM_USER";
        welcomeService.resendWelcome(TenantContext.requireTenantId(), userId, actorId);
    }
}
