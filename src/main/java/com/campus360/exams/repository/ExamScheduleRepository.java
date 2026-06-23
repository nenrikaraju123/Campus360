package com.campus360.exams.repository;

import com.campus360.exams.domain.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {

    Optional<ExamSchedule> findByIdAndTenantId(Long id, Long tenantId);

    List<ExamSchedule> findByTenantIdAndExamCycleId(Long tenantId, Long examCycleId);

    List<ExamSchedule> findByTenantIdAndExamCycleIdAndCourseId(Long tenantId, Long examCycleId, Long courseId);
}
