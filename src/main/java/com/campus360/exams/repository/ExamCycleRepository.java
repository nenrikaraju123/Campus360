package com.campus360.exams.repository;

import com.campus360.exams.domain.ExamCycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamCycleRepository extends JpaRepository<ExamCycle, Long> {

    Optional<ExamCycle> findByIdAndTenantId(Long id, Long tenantId);

    List<ExamCycle> findByTenantIdOrderByStartDateDesc(Long tenantId);

    List<ExamCycle> findByTenantIdAndStatus(Long tenantId, String status);
}
