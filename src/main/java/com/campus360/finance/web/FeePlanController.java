package com.campus360.finance.web;

import com.campus360.finance.domain.FeeCategory;
import com.campus360.finance.domain.FeeComponent;
import com.campus360.finance.service.FeePlanService;
import com.campus360.platform.tenancy.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
public class FeePlanController {

    private final FeePlanService feePlanService;

    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'FINANCE')")
    @GetMapping("/fee-categories")
    public List<FeeCategory> getCategories() {
        return feePlanService.getCategories(TenantContext.requireTenantId());
    }

    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'FINANCE')")
    @PostMapping("/fee-categories")
    public FeeCategory createCategory(@Valid @RequestBody FeeCategory category) {
        category.setTenantId(TenantContext.requireTenantId());
        return feePlanService.createCategory(category);
    }

    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'FINANCE')")
    @GetMapping("/fee-components")
    public List<FeeComponent> getComponents(@RequestParam Long categoryId) {
        return feePlanService.getComponents(TenantContext.requireTenantId(), categoryId);
    }

    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'FINANCE')")
    @PostMapping("/fee-components")
    public FeeComponent createComponent(@Valid @RequestBody FeeComponent component) {
        component.setTenantId(TenantContext.requireTenantId());
        return feePlanService.createComponent(component);
    }
}
