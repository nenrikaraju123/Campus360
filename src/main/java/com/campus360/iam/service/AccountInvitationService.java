package com.campus360.iam.service;

import com.campus360.iam.domain.AccountInvitation;
import com.campus360.iam.repository.AccountInvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountInvitationService {

    private final AccountInvitationRepository repository;

    @Transactional
    public void resendInvitation(Long tenantId, Long invitationId, String actorId) {
        AccountInvitation invitation = repository.findByTenantIdAndId(tenantId, invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (!"PENDING".equals(invitation.getStatus())) {
            throw new IllegalStateException("Can only resend pending invitations");
        }

        // Logic to trigger email resend would go here
    }

    @Transactional
    public void revokeInvitation(Long tenantId, Long invitationId, String actorId) {
        AccountInvitation invitation = repository.findByTenantIdAndId(tenantId, invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (!"PENDING".equals(invitation.getStatus())) {
            throw new IllegalStateException("Can only revoke pending invitations");
        }

        invitation.setStatus("REVOKED");
        repository.save(invitation);
    }
}
