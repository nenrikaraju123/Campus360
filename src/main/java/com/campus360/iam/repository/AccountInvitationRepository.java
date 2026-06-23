package com.campus360.iam.repository;

import com.campus360.iam.domain.AccountInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountInvitationRepository extends JpaRepository<AccountInvitation, Long> {
    Optional<AccountInvitation> findByTenantIdAndId(Long tenantId, Long id);
}
