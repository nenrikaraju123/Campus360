package com.campus360.iam.repository;

import com.campus360.iam.domain.AccountLockEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountLockEventRepository extends JpaRepository<AccountLockEvent, Long> {
    Optional<AccountLockEvent> findTopByUserIdOrderByLockedAtDesc(Long userId);
}
