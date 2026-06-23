package com.campus360.timetable.repository;

import com.campus360.timetable.domain.TimetableTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimetableTemplateRepository extends JpaRepository<TimetableTemplate, Long> {

    Optional<TimetableTemplate> findByIdAndTenantId(Long id, Long tenantId);

    List<TimetableTemplate> findByTenantIdAndTermId(Long tenantId, Long termId);
}
