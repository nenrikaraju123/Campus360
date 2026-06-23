package com.campus360.institution.repository;

import com.campus360.institution.domain.CurriculumItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CurriculumItemRepository extends JpaRepository<CurriculumItem, Long> {
    List<CurriculumItem> findByTenantIdAndProgramId(Long tenantId, Long programId);
}
