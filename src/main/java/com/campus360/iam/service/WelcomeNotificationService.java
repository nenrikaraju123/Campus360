package com.campus360.iam.service;

import com.campus360.iam.domain.WelcomeNotificationJob;
import com.campus360.iam.repository.WelcomeNotificationJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WelcomeNotificationService {

    private final WelcomeNotificationJobRepository repository;

    @Transactional
    public void scheduleWelcome(Long tenantId, Long userId, String templateName) {
        WelcomeNotificationJob job = new WelcomeNotificationJob();
        job.setTenantId(tenantId);
        // We'd load user from repository here in reality
        // job.setUser(userRepository.getReferenceById(userId));
        job.setTemplateName(templateName);
        repository.save(job);
    }
    
    @Transactional
    public void resendWelcome(Long tenantId, Long userId, String actorId) {
        // Find existing job and resend it
    }
}
