package com.campus360.notification.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "outbox_messages")
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId;

    @Column(nullable = false, length = 50)
    private String type; // EMAIL, SMS, NOTIFICATION

    @Column(nullable = false)
    private String recipient;

    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(nullable = false, length = 50)
    private String status = "PENDING"; // PENDING, SENT, FAILED

    @Column(nullable = false)
    private Integer retryCount = 0;

    private Instant nextRetryAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant processedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public Instant getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(Instant nextRetryAt) { this.nextRetryAt = nextRetryAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
