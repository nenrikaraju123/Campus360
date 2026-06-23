package com.campus360.importer.repository;

import com.campus360.importer.domain.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {
}
