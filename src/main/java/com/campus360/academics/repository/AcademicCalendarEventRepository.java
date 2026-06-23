package com.campus360.academics.repository;

import com.campus360.academics.domain.AcademicCalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AcademicCalendarEventRepository extends JpaRepository<AcademicCalendarEvent, Long> {

    Optional<AcademicCalendarEvent> findByIdAndTenantId(Long id, Long tenantId);

    List<AcademicCalendarEvent> findByTenantIdOrderByStartDateAsc(Long tenantId);

    List<AcademicCalendarEvent> findByTenantIdAndEventType(Long tenantId, String eventType);

    @Query("SELECT e FROM AcademicCalendarEvent e WHERE e.tenantId = :tenantId " +
           "AND ((e.endDate IS NULL AND e.startDate >= :start AND e.startDate <= :end) " +
           "OR (e.endDate IS NOT NULL AND e.startDate <= :end AND e.endDate >= :start)) " +
           "ORDER BY e.startDate ASC")
    List<AcademicCalendarEvent> findEventsInDateRange(Long tenantId, LocalDate start, LocalDate end);
}
