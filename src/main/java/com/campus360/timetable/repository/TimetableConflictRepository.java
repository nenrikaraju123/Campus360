package com.campus360.timetable.repository;

import com.campus360.timetable.domain.TimetableConflict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface TimetableConflictRepository extends JpaRepository<TimetableConflict, Long> {

    List<TimetableConflict> findByTenantIdAndTemplateId(Long tenantId, Long templateId);

    @Modifying
    void deleteByTenantIdAndTemplateId(Long tenantId, Long templateId);

    long countByTenantIdAndTemplateIdAndResolvedFalse(Long tenantId, Long templateId);
}
