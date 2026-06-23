package com.campus360.iam.repository;

import com.campus360.iam.domain.WelcomeNotificationJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WelcomeNotificationJobRepository extends JpaRepository<WelcomeNotificationJob, Long> {
    Optional<WelcomeNotificationJob> findByTenantIdAndId(Long tenantId, Long id);
}
