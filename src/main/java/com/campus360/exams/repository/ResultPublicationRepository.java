package com.campus360.exams.repository;

import com.campus360.exams.domain.ResultPublication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResultPublicationRepository extends JpaRepository<ResultPublication, Long> {

    Optional<ResultPublication> findByTenantIdAndExamCycleIdAndProgramId(
            Long tenantId, Long examCycleId, Long programId);
}
