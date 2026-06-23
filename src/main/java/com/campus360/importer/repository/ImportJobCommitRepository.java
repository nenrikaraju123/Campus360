package com.campus360.importer.repository;

import com.campus360.importer.domain.ImportJobCommit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportJobCommitRepository extends JpaRepository<ImportJobCommit, Long> {
    List<ImportJobCommit> findByTenantIdAndJob_Id(Long tenantId, Long jobId);
}
