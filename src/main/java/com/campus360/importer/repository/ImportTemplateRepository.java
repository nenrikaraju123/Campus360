package com.campus360.importer.repository;

import com.campus360.importer.domain.ImportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImportTemplateRepository extends JpaRepository<ImportTemplate, Long> {
    Optional<ImportTemplate> findByTenantIdAndType(Long tenantId, String type);
}
