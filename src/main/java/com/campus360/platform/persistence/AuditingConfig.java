package com.campus360.platform.persistence;

import com.campus360.platform.security.JwtAuthFilter.AuthPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class AuditingConfig {

    /** Supplies the current actor's email for @CreatedBy/@LastModifiedBy. */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.of("system");
            }
            Object principal = auth.getPrincipal();
            if (principal instanceof AuthPrincipal ap && ap.email() != null) {
                return Optional.of(ap.email());
            }
            return Optional.of(auth.getName() == null ? "system" : auth.getName());
        };
    }
}
