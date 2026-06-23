package com.campus360.onboarding.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** A public request to onboard a new institution, reviewed by the platform admin. */
@Entity
@Table(name = "tenant_registrations")
@Getter
@Setter
public class TenantRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_name", nullable = false, length = 200)
    private String institutionName;

    @Column(name = "institution_code", nullable = false, length = 40)
    private String institutionCode;

    @Column(nullable = false, length = 40)
    private String type = "UNIVERSITY";

    @Column(name = "admin_full_name", nullable = false, length = 160)
    private String adminFullName;

    @Column(name = "admin_email", nullable = false, length = 180)
    private String adminEmail;

    @Column(name = "contact_phone", length = 40)
    private String contactPhone;

    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    @Column(name = "review_notes", length = 1000)
    private String reviewNotes;

    @Column(name = "reviewed_by", length = 120)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "institution_id")
    private Long institutionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
