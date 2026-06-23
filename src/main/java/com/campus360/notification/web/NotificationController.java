package com.campus360.notification.web;

import com.campus360.notification.domain.Notification;
import com.campus360.notification.service.NotificationPersistenceService;
import com.campus360.notification.service.RealtimeNotificationService;
import com.campus360.platform.security.CurrentUser;
import com.campus360.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;


@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Real-time SSE stream + persistent inbox management")
public class NotificationController {

    private final RealtimeNotificationService realtimeService;
    private final NotificationPersistenceService persistenceService;

    public NotificationController(RealtimeNotificationService realtimeService,
                                  NotificationPersistenceService persistenceService) {
        this.realtimeService = realtimeService;
        this.persistenceService = persistenceService;
    }

    // ---- SSE stream ----
    /**
     * Subscribe to the live notification stream.
     * <ul>
     *   <li>Tenant users (INSTITUTION_ADMIN, FACULTY, STUDENT …) subscribe to their
     *       institution's stream, keyed by their tenantId.</li>
     *   <li>Platform users (SUPER_ADMIN, tenantId = null) subscribe to a special
     *       platform-level stream keyed by {@code null}. They receive events published
     *       with a null tenantId (e.g., new registration received).</li>
     * </ul>
     */
    @GetMapping("/stream")
    @Operation(summary = "Subscribe to the live notification stream (tenant-scoped or platform-level)")
    public SseEmitter stream() {
        Long tenantId = CurrentUser.tenantId(); // null for SUPER_ADMIN — that's fine
        return realtimeService.subscribe(tenantId);
    }

    // ---- Persistent inbox ----
    @GetMapping("/inbox")
    @Operation(summary = "Get the authenticated user's notification inbox (paginated)")
    public PageResponse<Notification> inbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean unreadOnly) {
        return persistenceService.inbox(page, size, unreadOnly);
    }

    @GetMapping("/tenant")
    @Operation(summary = "Get tenant-wide broadcast notifications")
    public PageResponse<Notification> tenantNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return persistenceService.tenantNotifications(page, size);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count for the authenticated user")
    public Map<String, Long> unreadCount() {
        return persistenceService.unreadCount();
    }

    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark a notification as read")
    public void markRead(@PathVariable Long id) {
        persistenceService.markRead(id);
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public Map<String, Integer> markAllRead() {
        return persistenceService.markAllRead();
    }
}
