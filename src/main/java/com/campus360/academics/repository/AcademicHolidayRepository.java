package com.campus360.academics.repository;

import com.campus360.academics.domain.AcademicHoliday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AcademicHolidayRepository extends JpaRepository<AcademicHoliday, Long> {

    Optional<AcademicHoliday> findByIdAndTenantId(Long id, Long tenantId);

    List<AcademicHoliday> findByTenantIdOrderByHolidayDateAsc(Long tenantId);

    List<AcademicHoliday> findByTenantIdAndHolidayDateBetweenOrderByHolidayDateAsc(
            Long tenantId, LocalDate start, LocalDate end);
}
