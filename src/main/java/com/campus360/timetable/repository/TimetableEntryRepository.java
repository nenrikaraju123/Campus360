package com.campus360.timetable.repository;

import com.campus360.timetable.domain.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {

    Optional<TimetableEntry> findByIdAndTenantId(Long id, Long tenantId);

    List<TimetableEntry> findByTenantIdAndTemplateId(Long tenantId, Long templateId);

    List<TimetableEntry> findByTenantIdAndTemplateIdAndSectionId(Long tenantId, Long templateId, Long sectionId);

    List<TimetableEntry> findByTenantIdAndTemplateIdAndFacultyId(Long tenantId, Long templateId, Long facultyId);

    List<TimetableEntry> findByTenantIdAndTemplateIdAndRoomId(Long tenantId, Long templateId, Long roomId);
}
