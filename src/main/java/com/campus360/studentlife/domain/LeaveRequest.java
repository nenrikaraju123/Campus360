package com.campus360.studentlife.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;

/** Leave / exemption request from a student. */
@Entity
@Table(name = "leave_requests")
@Getter
@Setter
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "section_id")
    private Long sectionId;

    /** MEDICAL, PERSONAL, ACADEMIC, OTHER */
    @Column(name = "leave_type", nullable = false, length = 30)
    private String leaveType = "PERSONAL";

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(length = 500)
    private String reason;

    @Column(name = "supporting_doc", length = 500)
    private String supportingDoc;

    /** PENDING, APPROVED, REJECTED */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "reviewed_by", length = 120)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
