package com.campus360.academics.repository;

import com.campus360.academics.domain.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByMeetingId(Long meetingId);

    List<AttendanceRecord> findByEnrollmentId(Long enrollmentId);

    boolean existsByEnrollmentIdAndMeetingId(Long enrollmentId, Long meetingId);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.enrollmentId = :enrollmentId AND a.status = 'PRESENT'")
    long countPresentByEnrollment(@Param("enrollmentId") Long enrollmentId);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.enrollmentId = :enrollmentId")
    long countTotalByEnrollment(@Param("enrollmentId") Long enrollmentId);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.enrollmentId = :enrollmentId AND a.status IN ('PRESENT','LATE')")
    long countAttendedByEnrollment(@Param("enrollmentId") Long enrollmentId);
}
