package com.campus360.academics.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Per-student, per-meeting attendance record. Source-agnostic (manual, QR, biometric). */
@Entity
@Table(name = "attendance_records")
@Getter
@Setter
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;

    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    /** PRESENT, ABSENT, LATE, EXCUSED */
    @Column(nullable = false, length = 20)
    private String status = "PRESENT";

    /** MANUAL, QR, BIOMETRIC */
    @Column(length = 30)
    private String source = "MANUAL";

    @Column(name = "marked_at")
    private Instant markedAt = Instant.now();

    @Column(name = "marked_by", length = 120)
    private String markedBy;
}
