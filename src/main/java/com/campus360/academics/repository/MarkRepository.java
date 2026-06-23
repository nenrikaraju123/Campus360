package com.campus360.academics.repository;

import com.campus360.academics.domain.Mark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarkRepository extends JpaRepository<Mark, Long> {

    Optional<Mark> findByAssessmentIdAndEnrollmentId(Long assessmentId, Long enrollmentId);

    List<Mark> findByAssessmentId(Long assessmentId);

    List<Mark> findByEnrollmentId(Long enrollmentId);

    boolean existsByAssessmentIdAndEnrollmentId(Long assessmentId, Long enrollmentId);
}
