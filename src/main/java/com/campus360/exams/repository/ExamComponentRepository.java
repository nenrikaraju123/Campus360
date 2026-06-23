package com.campus360.exams.repository;

import com.campus360.exams.domain.ExamComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamComponentRepository extends JpaRepository<ExamComponent, Long> {

    Optional<ExamComponent> findByIdAndTenantId(Long id, Long tenantId);

    List<ExamComponent> findByTenantIdAndExamCycleId(Long tenantId, Long examCycleId);

    List<ExamComponent> findByTenantIdAndExamCycleIdAndCourseId(Long tenantId, Long examCycleId, Long courseId);
}
