package com.campus360.notification.repository;

import com.campus360.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndReadOrderByCreatedAtDesc(Long userId, boolean read, Pageable pageable);

    Page<Notification> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    long countByUserIdAndRead(Long userId, boolean read);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :now WHERE n.userId = :userId AND n.read = false")
    int markAllReadForUser(@Param("userId") Long userId, @Param("now") java.time.Instant now);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :now WHERE n.id = :id AND n.userId = :userId")
    int markRead(@Param("id") Long id, @Param("userId") Long userId, @Param("now") java.time.Instant now);
}
