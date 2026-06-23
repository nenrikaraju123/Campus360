package com.campus360.exams.repository;

import com.campus360.exams.domain.ExamMarkSheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamMarkSheetRepository extends JpaRepository<ExamMarkSheet, Long> {

    Optional<ExamMarkSheet> findByIdAndTenantId(Long id, Long tenantId);

    Optional<ExamMarkSheet> findByTenantIdAndExamCycleIdAndCourseIdAndSectionId(
            Long tenantId, Long examCycleId, Long courseId, Long sectionId);

    List<ExamMarkSheet> findByTenantIdAndExamCycleIdAndFacultyId(Long tenantId, Long examCycleId, Long facultyId);

    List<ExamMarkSheet> findByTenantIdAndExamCycleIdAndStatus(Long tenantId, Long examCycleId, String status);
}
