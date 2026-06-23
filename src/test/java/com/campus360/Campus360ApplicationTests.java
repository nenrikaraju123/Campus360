package com.campus360;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: boots the full application context against in-memory H2, verifying
 * that every bean (security, tenancy, JPA mappings, Spring AI, all modules) wires
 * up. Hibernate create-drop also validates the entity model is internally consistent.
 */
@SpringBootTest
@ActiveProfiles("test")
class Campus360ApplicationTests {

    @Test
    void contextLoads() {
        // Context startup is the assertion.
    }
}
