package com.campus360.notification.domain;

import java.time.Instant;

/**
 * In-process domain event broadcast to a tenant's live subscribers. Today it is
 * delivered over SSE; the same event can later be fanned out to email/SMS/push
 * or published to Kafka without changing producers.
 */
public record NotificationEvent(
        Long tenantId,
        String type,
        String title,
        String message,
        Instant at) {

    public static NotificationEvent of(Long tenantId, String type, String title, String message) {
        return new NotificationEvent(tenantId, type, title, message, Instant.now());
    }
}
