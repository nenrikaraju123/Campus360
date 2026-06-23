package com.campus360.academics.repository;

import com.campus360.academics.domain.TermResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermResultRepository extends JpaRepository<TermResult, Long> {

    Optional<TermResult> findByStudentIdAndTermId(Long studentId, Long termId);

    List<TermResult> findByTenantIdAndStudentIdOrderByTermIdAsc(Long tenantId, Long studentId);

    List<TermResult> findByTenantIdAndTermId(Long tenantId, Long termId);
}
