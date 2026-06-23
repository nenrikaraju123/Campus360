package com.campus360.iam;

import com.campus360.iam.web.AuthController;
import com.campus360.iam.web.dto.LoginRequest;
import com.campus360.platform.security.JwtService;
import com.campus360.testsupport.TestTenantFactory;
import com.campus360.testsupport.TestUserFactory;
import com.campus360.iam.domain.User;
import com.campus360.institution.domain.Institution;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestTenantFactory tenantFactory;

    @Autowired
    private TestUserFactory userFactory;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        Institution tenant = tenantFactory.createTenant("Test Uni");
        User user = userFactory.createUser(tenant.getId(), "INSTITUTION_ADMIN");
        // Update password to something known
        user.setPasswordHash(passwordEncoder.encode("password123"));

        LoginRequest req = new LoginRequest(tenant.getCode(), user.getEmail(), "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(user.getId().intValue()));
    }

    @Test
    void shouldFailLoginWithBadPassword() throws Exception {
        Institution tenant = tenantFactory.createTenant("Test Uni");
        User user = userFactory.createUser(tenant.getId(), "INSTITUTION_ADMIN");
        user.setPasswordHash(passwordEncoder.encode("password123"));

        LoginRequest req = new LoginRequest(tenant.getCode(), user.getEmail(), "wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
