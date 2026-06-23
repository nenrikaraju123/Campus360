package com.campus360.notification.service;

import com.campus360.notification.domain.OutboxMessage;
import com.campus360.notification.repository.OutboxMessageRepository;
import com.campus360.platform.mail.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class OutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(OutboxProcessor.class);

    private final OutboxMessageRepository repository;
    private final MailService mailService;

    public OutboxProcessor(OutboxMessageRepository repository, MailService mailService) {
        this.repository = repository;
        this.mailService = mailService;
    }

    @Scheduled(fixedDelayString = "${campus360.outbox.poll-delay:30000}")
    @Transactional
    public void processOutbox() {
        List<OutboxMessage> pending = repository.findPendingMessages(Instant.now(), PageRequest.of(0, 50));
        
        for (OutboxMessage message : pending) {
            try {
                if ("EMAIL".equalsIgnoreCase(message.getType())) {
                    mailService.sendThrowing(message.getRecipient(), message.getSubject(), message.getPayload());
                    
                    message.setStatus("SENT");
                    message.setProcessedAt(Instant.now());
                } else {
                    message.setStatus("FAILED");
                    message.setErrorMessage("Unsupported message type: " + message.getType());
                }
            } catch (Exception e) {
                log.error("Failed to process outbox message {}", message.getId(), e);
                int retries = message.getRetryCount() + 1;
                message.setRetryCount(retries);
                
                if (retries >= 5) {
                    message.setStatus("FAILED");
                } else {
                    // Exponential backoff
                    message.setNextRetryAt(Instant.now().plus(retries * 5L, ChronoUnit.MINUTES));
                }
                message.setErrorMessage(e.getMessage());
            }
            repository.save(message);
        }
    }
    
    @Transactional
    public void scheduleEmail(Long tenantId, String to, String subject, String body) {
        OutboxMessage msg = new OutboxMessage();
        msg.setTenantId(tenantId);
        msg.setType("EMAIL");
        msg.setRecipient(to);
        msg.setSubject(subject);
        msg.setPayload(body);
        repository.save(msg);
    }
}
