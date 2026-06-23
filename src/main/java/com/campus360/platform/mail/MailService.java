package com.campus360.platform.mail;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Best-effort transactional email. If no mail sender is configured (e.g. local
 * dev without Mailhog), it logs instead of failing the calling operation —
 * provisioning must never be blocked by mail delivery.
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;

    @Value("${campus360.mail.from:noreply@campus360.local}")
    private String fromAddress;

    public MailService(ObjectProvider<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    public void send(String to, String subject, String body) {
        try {
            sendThrowing(to, subject, body);
        } catch (Exception e) {
            log.warn("Failed to send email to {} ({}): {}", to, subject, e.getMessage());
        }
    }

    public void sendThrowing(String to, String subject, String body) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("[mail disabled] To: {} | Subject: {}\n{}", to, subject, body);
            return;
        }
        
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            
            boolean isHtml = body != null && body.trim().startsWith("<");
            helper.setText(body, isHtml);
            
            sender.send(message);
            log.info("Sent email to {} ({})", to, subject);
        } catch (Exception e) {
            throw new RuntimeException("Mail delivery failed", e);
        }
    }
}
