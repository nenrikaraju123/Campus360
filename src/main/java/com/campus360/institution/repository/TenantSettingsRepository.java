package com.campus360.institution.repository;

import com.campus360.institution.domain.TenantSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TenantSettingsRepository extends JpaRepository<TenantSetting, Long> {
    Optional<TenantSetting> findByTenantId(Long tenantId);
}
