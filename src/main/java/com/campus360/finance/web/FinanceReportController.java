package com.campus360.finance.web;

import com.campus360.finance.service.FinanceReportService;
import com.campus360.platform.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/finance/reports")
@RequiredArgsConstructor
public class FinanceReportController {

    private final FinanceReportService reportService;

    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'FINANCE')")
    @GetMapping("/collection-summary")
    public Map<String, Object> getCollectionSummary() {
        return reportService.getCollectionSummary(TenantContext.requireTenantId());
    }

    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'FINANCE')")
    @GetMapping("/due-aging")
    public Map<String, Object> getDueAging() {
        return reportService.getDueAging(TenantContext.requireTenantId());
    }
}
