package com.campus360.iam.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "account_lock_events")
public class AccountLockEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private Instant lockedAt;

    private Instant unlockedAt;
    
    private String unlockedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Instant getLockedAt() { return lockedAt; }
    public void setLockedAt(Instant lockedAt) { this.lockedAt = lockedAt; }

    public Instant getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(Instant unlockedAt) { this.unlockedAt = unlockedAt; }

    public String getUnlockedBy() { return unlockedBy; }
    public void setUnlockedBy(String unlockedBy) { this.unlockedBy = unlockedBy; }
}
