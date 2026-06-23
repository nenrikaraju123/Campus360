package com.campus360.institution.repository;

import com.campus360.institution.domain.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findByTenantId(Long tenantId);

    List<Section> findByTenantIdAndTermId(Long tenantId, Long termId);

    Optional<Section> findByIdAndTenantId(Long id, Long tenantId);
}
