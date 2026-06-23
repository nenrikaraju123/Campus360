package com.campus360.finance.service;

import com.campus360.finance.domain.FeeCategory;
import com.campus360.finance.domain.FeeComponent;
import com.campus360.finance.repository.FeeCategoryRepository;
import com.campus360.finance.repository.FeeComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeePlanService {

    private final FeeCategoryRepository feeCategoryRepository;
    private final FeeComponentRepository feeComponentRepository;

    @Transactional(readOnly = true)
    public List<FeeCategory> getCategories(Long tenantId) {
        return feeCategoryRepository.findByTenantId(tenantId);
    }

    @Transactional
    public FeeCategory createCategory(FeeCategory category) {
        return feeCategoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<FeeComponent> getComponents(Long tenantId, Long categoryId) {
        return feeComponentRepository.findByCategory_IdAndTenantId(categoryId, tenantId);
    }

    @Transactional
    public FeeComponent createComponent(FeeComponent component) {
        return feeComponentRepository.save(component);
    }
}
