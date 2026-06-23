package com.campus360.iam.service;

import com.campus360.iam.domain.AccountLockEvent;
import com.campus360.iam.domain.UserLoginEvent;
import com.campus360.iam.repository.AccountLockEventRepository;
import com.campus360.iam.repository.UserLoginEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
@Transactional
public class LoginAuditService {

    private final UserLoginEventRepository loginEvents;
    private final AccountLockEventRepository lockEvents;

    public LoginAuditService(UserLoginEventRepository loginEvents, AccountLockEventRepository lockEvents) {
        this.loginEvents = loginEvents;
        this.lockEvents = lockEvents;
    }

    public void logSuccess(Long userId, String email, Long tenantId, String ipAddress, String userAgent) {
        logEvent(userId, email, tenantId, "SUCCESS", ipAddress, userAgent);
    }

    public void logFailure(Long userId, String email, Long tenantId, String ipAddress, String userAgent) {
        logEvent(userId, email, tenantId, "FAILED", ipAddress, userAgent);
        checkAndLockAccount(userId);
    }

    private void logEvent(Long userId, String email, Long tenantId, String status, String ipAddress, String userAgent) {
        UserLoginEvent event = new UserLoginEvent();
        event.setUserId(userId);
        event.setEmail(email);
        event.setTenantId(tenantId);
        event.setStatus(status);
        event.setIpAddress(ipAddress);
        event.setUserAgent(userAgent);
        loginEvents.save(event);
    }

    private void checkAndLockAccount(Long userId) {
        if (userId == null) return;
        
        long recentFailures = loginEvents.findTop10ByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(5)
                .filter(e -> "FAILED".equals(e.getStatus()))
                .count();

        if (recentFailures >= 5) {
            AccountLockEvent lock = new AccountLockEvent();
            lock.setUserId(userId);
            lock.setReason("Exceeded maximum consecutive failed login attempts");
            lock.setLockedAt(Instant.now());
            lockEvents.save(lock);
            
            logEvent(userId, "System", null, "LOCKED", "System", "Account locked due to failures");
        }
    }

    @Transactional(readOnly = true)
    public Page<UserLoginEvent> getLoginHistory(Long userId, int page, int size) {
        return loginEvents.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }
}
