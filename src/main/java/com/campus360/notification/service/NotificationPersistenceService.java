package com.campus360.notification.service;

import com.campus360.notification.domain.Notification;
import com.campus360.notification.domain.NotificationEvent;
import com.campus360.notification.repository.NotificationRepository;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.shared.dto.PageResponse;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Persistent notification service. Listens for {@link NotificationEvent}s,
 * persists them, and exposes inbox/read/unread management to the user.
 */
@Service
@Transactional
public class NotificationPersistenceService {

    private final NotificationRepository notifications;

    public NotificationPersistenceService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    /** Persist every NotificationEvent as an in-app notification for the tenant. */
    @EventListener
    public void onNotification(NotificationEvent event) {
        Notification n = new Notification();
        n.setTenantId(event.tenantId());
        n.setType(event.type());
        n.setTitle(event.title());
        n.setMessage(event.message());
        n.setChannel("IN_APP");
        // tenant-wide notification; userId left null = "broadcast to tenant"
        notifications.save(n);
    }

    /** Create a targeted notification for a specific user. */
    public Notification notify(Long userId, Long tenantId, String type, String title, String message,
                               String refType, Long refId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTenantId(tenantId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setRefType(refType);
        n.setRefId(refId);
        return notifications.save(n);
    }

    // ---- Inbox ----
    @Transactional(readOnly = true)
    public PageResponse<Notification> inbox(int page, int size, Boolean unreadOnly) {
        Long userId = CurrentUser.id();
        if (userId == null) throw ApiException.forbidden("Authentication required");

        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        if (Boolean.TRUE.equals(unreadOnly)) {
            return PageResponse.of(notifications.findByUserIdAndReadOrderByCreatedAtDesc(userId, false, pageable));
        }
        return PageResponse.of(notifications.findByUserIdOrderByCreatedAtDesc(userId, pageable));
    }

    @Transactional(readOnly = true)
    public PageResponse<Notification> tenantNotifications(int page, int size) {
        Long tenantId = TenantContext.getTenantId();
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return PageResponse.of(notifications.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> unreadCount() {
        Long userId = CurrentUser.id();
        if (userId == null) return Map.of("unread", 0L);
        return Map.of("unread", notifications.countByUserIdAndRead(userId, false));
    }

    public void markRead(Long notificationId) {
        Long userId = CurrentUser.id();
        if (userId == null) throw ApiException.forbidden("Authentication required");
        notifications.markRead(notificationId, userId, java.time.Instant.now());
    }

    public Map<String, Integer> markAllRead() {
        Long userId = CurrentUser.id();
        if (userId == null) throw ApiException.forbidden("Authentication required");
        int count = notifications.markAllReadForUser(userId, java.time.Instant.now());
        return Map.of("marked", count);
    }
}
