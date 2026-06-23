package com.campus360.importer.repository;

import com.campus360.importer.domain.ImportJobError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportJobErrorRepository extends JpaRepository<ImportJobError, Long> {
    List<ImportJobError> findByTenantIdAndJob_Id(Long tenantId, Long jobId);
}
