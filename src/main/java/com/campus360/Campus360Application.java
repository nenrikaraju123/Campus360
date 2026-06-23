package com.campus360;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Campus360 — Enterprise academic, student & placement platform.
 *
 * Modular monolith: each top-level package under com.campus360 is a bounded
 * context (iam, institution, student, placement, ai, platform). Cross-module
 * collaboration happens through public service interfaces, not by reaching into
 * another module's repositories.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@org.springframework.scheduling.annotation.EnableScheduling
public class Campus360Application {

    public static void main(String[] args) {
        SpringApplication.run(Campus360Application.class, args);
    }
}
