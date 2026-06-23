package com.campus360.admissions;

import com.campus360.admissions.domain.AdmissionApplication;
import com.campus360.admissions.repository.AdmissionApplicationRepository;
import com.campus360.admissions.web.dto.AdmissionApplicationRequest;
import com.campus360.institution.domain.Institution;
import com.campus360.testsupport.TestTenantFactory;
import com.campus360.testsupport.WithMockCampusUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that Tenant A's admission data is never accessible from Tenant B's context.
 * Each @WithMockCampusUser defaults tenantId=1; we deliberately seed data in tenantId=999
 * and verify it is not returned to tenantId=1.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AdmissionTenantIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestTenantFactory tenantFactory;

    @Autowired
    private AdmissionApplicationRepository applicationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Seed an application for tenant 999 (different from WithMockCampusUser default of tenantId=1)
        AdmissionApplication app = new AdmissionApplication();
        app.setTenantId(999L);
        app.setFirstName("Secret");
        app.setLastName("Applicant");
        app.setEmail("secret@tenant999.com");
        app.setApplicationNumber("ADM-SEED-001");
        applicationRepository.save(app);
    }

    @Test
    @WithMockCampusUser(role = "INSTITUTION_ADMIN", tenantId = 1L)
    void tenantOneCannotSeeTenantTwoApplications() throws Exception {
        // Tenant 1 list should not include tenant 999's application
        mockMvc.perform(get("/api/v1/admissions/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.email == 'secret@tenant999.com')]").isEmpty());
    }

    @Test
    @WithMockCampusUser(role = "INSTITUTION_ADMIN", tenantId = 1L)
    void tenantOneCannotGetTenantTwoApplicationById() throws Exception {
        // Find the seeded tenant-999 application
        AdmissionApplication seeded = applicationRepository
                .findAll()
                .stream()
                .filter(a -> "secret@tenant999.com".equals(a.getEmail()))
                .findFirst()
                .orElseThrow();

        // Attempt to GET it as tenant 1 — should return 404
        mockMvc.perform(get("/api/v1/admissions/applications/" + seeded.getId()))
                .andExpect(status().isNotFound());
    }
}
