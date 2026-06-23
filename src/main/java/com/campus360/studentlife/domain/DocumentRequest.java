package com.campus360.studentlife.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

/** Document request (transcript, bonafide, migration, etc.) */
@Entity
@Table(name = "document_requests")
@Getter
@Setter
public class DocumentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    /** TRANSCRIPT, BONAFIDE, MIGRATION, CHARACTER, DEGREE */
    @Column(name = "doc_type", nullable = false, length = 40)
    private String docType;

    @Column(length = 300)
    private String purpose;

    @Column(nullable = false)
    private int copies = 1;

    /** PENDING, PROCESSING, READY, COLLECTED, REJECTED */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "document_ref", length = 500)
    private String documentRef;

    @Column(name = "reviewed_by", length = 120)
    private String reviewedBy;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();
}
