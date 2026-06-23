package com.campus360.exams.repository;

import com.campus360.exams.domain.GradeCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeCardRepository extends JpaRepository<GradeCard, Long> {

    Optional<GradeCard> findByTenantIdAndStudentIdAndExamCycleId(Long tenantId, Long studentId, Long examCycleId);

    List<GradeCard> findByTenantIdAndStudentId(Long tenantId, Long studentId);

    List<GradeCard> findByTenantIdAndExamCycleId(Long tenantId, Long examCycleId);
}
