package com.campus360.academics.repository;

import com.campus360.academics.domain.ClassMeeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClassMeetingRepository extends JpaRepository<ClassMeeting, Long> {

    Optional<ClassMeeting> findByIdAndTenantId(Long id, Long tenantId);

    List<ClassMeeting> findBySectionIdOrderByMeetingDateDesc(Long sectionId);

    List<ClassMeeting> findBySectionIdAndMeetingDateBetween(Long sectionId, LocalDate start, LocalDate end);

    long countBySectionId(Long sectionId);
}
