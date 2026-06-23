package com.campus360.platform.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Immutable audit trail entry for security-sensitive and business-critical
 * mutations (grade changes, offer decisions, financial transactions, role
 * assignments). Enterprise compliance requirement.
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_email", length = 180)
    private String actorEmail;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 80)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
