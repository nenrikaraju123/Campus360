package com.campus360.institution.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tenant_settings")
public class TenantSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long tenantId;

    private String logoUrl;
    private String academicYear;
    private String gradingMode;
    private Double attendanceMinimumPercentage;
    private Integer feeDueReminderDays;

    @Column(columnDefinition = "JSONB")
    private String placementEligibilityDefaults;

    @Column(columnDefinition = "JSONB")
    private String notificationPreferences;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getGradingMode() { return gradingMode; }
    public void setGradingMode(String gradingMode) { this.gradingMode = gradingMode; }

    public Double getAttendanceMinimumPercentage() { return attendanceMinimumPercentage; }
    public void setAttendanceMinimumPercentage(Double attendanceMinimumPercentage) { this.attendanceMinimumPercentage = attendanceMinimumPercentage; }

    public Integer getFeeDueReminderDays() { return feeDueReminderDays; }
    public void setFeeDueReminderDays(Integer feeDueReminderDays) { this.feeDueReminderDays = feeDueReminderDays; }

    public String getPlacementEligibilityDefaults() { return placementEligibilityDefaults; }
    public void setPlacementEligibilityDefaults(String placementEligibilityDefaults) { this.placementEligibilityDefaults = placementEligibilityDefaults; }

    public String getNotificationPreferences() { return notificationPreferences; }
    public void setNotificationPreferences(String notificationPreferences) { this.notificationPreferences = notificationPreferences; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
