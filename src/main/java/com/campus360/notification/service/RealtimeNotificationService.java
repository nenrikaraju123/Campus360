package com.campus360.notification.service;

import com.campus360.notification.domain.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Real-time fan-out of {@link NotificationEvent}s to connected clients over SSE,
 * scoped per tenant so an institution only receives its own events.
 *
 * <p><b>Platform stream (SUPER_ADMIN):</b> Events published with {@code tenantId = null}
 * are delivered to the platform stream, keyed internally by {@link #PLATFORM_TENANT_KEY}.
 * This lets the platform admin's browser receive live notifications (new registrations, etc.)
 * without a tenant context.
 *
 * <p>For multi-instance deployments this in-memory map is the seam to back with a
 * Redis / Kafka pub-sub broker.
 */
@Service
public class RealtimeNotificationService {

    private static final Logger log = LoggerFactory.getLogger(RealtimeNotificationService.class);

    /**
     * Sentinel key used for the platform-level stream (SUPER_ADMIN, tenantId = null).
     * {@code ConcurrentHashMap} does not allow null keys, so we use -1 as a stand-in.
     */
    private static final Long PLATFORM_TENANT_KEY = -1L;

    /** Map of tenantId → list of connected SSE emitters. */
    private final Map<Long, List<SseEmitter>> emittersByTenant = new ConcurrentHashMap<>();

    /**
     * Subscribe to the live stream.
     *
     * @param tenantId the caller's tenant ID, or {@code null} for platform users (SUPER_ADMIN)
     */
    public SseEmitter subscribe(Long tenantId) {
        Long key = tenantId != null ? tenantId : PLATFORM_TENANT_KEY;
        SseEmitter emitter = new SseEmitter(0L); // never time out — client manages reconnect
        emittersByTenant.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(key, emitter));
        emitter.onTimeout(() -> remove(key, emitter));
        emitter.onError(e -> remove(key, emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data("subscribed"));
        } catch (IOException e) {
            remove(key, emitter);
        }
        log.debug("SSE subscriber added for tenant-key={}, total={}", key,
                emittersByTenant.getOrDefault(key, List.of()).size());
        return emitter;
    }

    /**
     * Producers publish a Spring application event; this listener fans it out
     * to all subscribers of the relevant tenant (or the platform stream for null tenant).
     */
    @EventListener
    public void onNotification(NotificationEvent event) {
        Long key = event.tenantId() != null ? event.tenantId() : PLATFORM_TENANT_KEY;
        List<SseEmitter> emitters = emittersByTenant.getOrDefault(key, List.of());
        if (emitters.isEmpty()) {
            log.debug("No SSE subscribers for tenant-key={}, event={}", key, event.type());
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(event.type()).data(event));
            } catch (IOException e) {
                log.debug("SSE write failed for tenant-key={}, removing emitter: {}", key, e.getMessage());
                remove(key, emitter);
            }
        }
        log.debug("Dispatched {} to {} subscriber(s) of tenant-key={}", event.type(), emitters.size(), key);
    }

    private void remove(Long key, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByTenant.get(key);
        if (emitters != null) {
            emitters.remove(emitter);
        }
    }
}
