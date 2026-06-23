package com.campus360.analytics.web;

import com.campus360.analytics.service.AnalyticsService;
import com.campus360.analytics.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics & Reporting", description = "Dashboards, KPIs, at-risk detection")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    @Operation(summary = "Institution-level dashboard with key metrics")
    public InstitutionDashboard dashboard() {
        return service.institutionDashboard();
    }

    @GetMapping("/at-risk")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    @Operation(summary = "Students flagged as at-risk (low CGPA, backlogs, attendance, overdue fees)")
    public List<AtRiskStudent> atRisk() {
        return service.atRiskStudents();
    }
}
