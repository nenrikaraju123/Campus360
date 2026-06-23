package com.campus360.exams.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "result_status_history")
@Getter
@Setter
public class ResultStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /** MARK_SHEET, GRADE_CARD */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "previous_status", length = 50)
    private String previousStatus;

    @Column(name = "new_status", nullable = false, length = 50)
    private String newStatus;

    @Column(name = "changed_by", nullable = false, length = 120)
    private String changedBy;

    @Column(length = 500)
    private String comments;

    @Column(name = "changed_at", updatable = false)
    private Instant changedAt = Instant.now();
}
