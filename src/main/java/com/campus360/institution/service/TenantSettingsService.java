package com.campus360.institution.service;

import com.campus360.institution.domain.TenantSetting;
import com.campus360.institution.repository.TenantSettingsRepository;
import com.campus360.platform.tenancy.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class TenantSettingsService {

    private final TenantSettingsRepository repository;

    public TenantSettingsService(TenantSettingsRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public TenantSetting getSettings() {
        Long tenantId = TenantContext.requireTenantId();
        return repository.findByTenantId(tenantId)
                .orElseGet(() -> {
                    TenantSetting defaultSetting = new TenantSetting();
                    defaultSetting.setTenantId(tenantId);
                    return defaultSetting;
                });
    }

    public TenantSetting updateSettings(TenantSetting newSettings) {
        Long tenantId = TenantContext.requireTenantId();
        TenantSetting existing = repository.findByTenantId(tenantId).orElse(new TenantSetting());
        
        existing.setTenantId(tenantId);
        existing.setLogoUrl(newSettings.getLogoUrl());
        existing.setAcademicYear(newSettings.getAcademicYear());
        existing.setGradingMode(newSettings.getGradingMode());
        existing.setAttendanceMinimumPercentage(newSettings.getAttendanceMinimumPercentage());
        existing.setFeeDueReminderDays(newSettings.getFeeDueReminderDays());
        existing.setPlacementEligibilityDefaults(newSettings.getPlacementEligibilityDefaults());
        existing.setNotificationPreferences(newSettings.getNotificationPreferences());
        existing.setUpdatedAt(Instant.now());

        return repository.save(existing);
    }
}
