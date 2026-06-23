package com.campus360.timetable.repository;

import com.campus360.timetable.domain.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    Optional<TimeSlot> findByIdAndTenantId(Long id, Long tenantId);

    List<TimeSlot> findByTenantIdOrderByDisplayOrderAsc(Long tenantId);

    Optional<TimeSlot> findByTenantIdAndDayOfWeekAndStartTimeAndEndTime(
            Long tenantId, String dayOfWeek, LocalTime startTime, LocalTime endTime);
}
