package com.campus360.exams.repository;

import com.campus360.exams.domain.ExamMark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamMarkRepository extends JpaRepository<ExamMark, Long> {

    List<ExamMark> findByTenantIdAndMarkSheetId(Long tenantId, Long markSheetId);

    Optional<ExamMark> findByTenantIdAndMarkSheetIdAndStudentIdAndExamComponentId(
            Long tenantId, Long markSheetId, Long studentId, Long examComponentId);
}
