package com.campus360.notification.repository;

import com.campus360.notification.domain.OutboxMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {
    
    @Query("SELECT o FROM OutboxMessage o WHERE o.status = 'PENDING' AND o.nextRetryAt <= :now ORDER BY o.createdAt ASC")
    List<OutboxMessage> findPendingMessages(Instant now, Pageable pageable);
}
