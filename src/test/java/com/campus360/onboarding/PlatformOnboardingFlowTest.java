package com.campus360.onboarding;

import com.campus360.onboarding.web.CreateInstitutionRequest;
import com.campus360.testsupport.WithMockCampusUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PlatformOnboardingFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.campus360.iam.repository.RoleRepository roleRepository;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        if (roleRepository.findByName("INSTITUTION_ADMIN").isEmpty()) {
            com.campus360.iam.domain.Role role = new com.campus360.iam.domain.Role();
            role.setName("INSTITUTION_ADMIN");
            roleRepository.save(role);
        }
    }

    @Test
    @WithMockCampusUser(role = "SUPER_ADMIN", email = "admin@campus360.local")
    void shouldCreateTenantWhenSuperAdmin() throws Exception {
        CreateInstitutionRequest req = new CreateInstitutionRequest(
                "New University", "NEWUNI", "UNIVERSITY", "Admin Name", "newuni@test.com", "password123");

        mockMvc.perform(post("/api/v1/platform/institutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.institutionCode").value("NEWUNI"));
    }

    @Test
    @WithMockCampusUser(role = "INSTITUTION_ADMIN")
    void shouldFailCreateTenantWhenNotSuperAdmin() throws Exception {
        CreateInstitutionRequest req = new CreateInstitutionRequest(
                "New University", "NEWUNI", "UNIVERSITY", "Admin Name", "newuni@test.com", "password123");

        mockMvc.perform(post("/api/v1/platform/institutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
