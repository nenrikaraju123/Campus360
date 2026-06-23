package com.campus360.academics.repository;

import com.campus360.academics.domain.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    Optional<Assessment> findByIdAndTenantId(Long id, Long tenantId);

    List<Assessment> findByTenantIdAndSectionId(Long tenantId, Long sectionId);
}
