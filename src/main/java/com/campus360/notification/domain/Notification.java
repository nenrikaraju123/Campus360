package com.campus360.notification.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Persistent notification stored in the database. Allows inbox-style
 * read/unread management and history queries.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 60)
    private String type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String message;

    /** IN_APP, EMAIL, SMS, PUSH */
    @Column(nullable = false, length = 20)
    private String channel = "IN_APP";

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "read_at")
    private Instant readAt;

    /** Optional reference to the entity this notification is about. */
    @Column(name = "ref_type", length = 60)
    private String refType;

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
