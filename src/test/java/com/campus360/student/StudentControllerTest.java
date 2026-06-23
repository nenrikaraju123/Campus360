package com.campus360.student;

import com.campus360.student.web.CreateStudentRequest;
import com.campus360.testsupport.TestTenantFactory;
import com.campus360.testsupport.WithMockCampusUser;
import com.campus360.institution.domain.Institution;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestTenantFactory tenantFactory;

    @Autowired
    private ObjectMapper objectMapper;

    private Long tenantId;

    @Autowired
    private com.campus360.iam.repository.RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        Institution tenant = tenantFactory.createTenant("Test Uni");
        tenantId = tenant.getId();
        
        if (roleRepository.findByName("STUDENT").isEmpty()) {
            com.campus360.iam.domain.Role role = new com.campus360.iam.domain.Role();
            role.setName("STUDENT");
            roleRepository.save(role);
        }
    }

    @Test
    @WithMockCampusUser(role = "INSTITUTION_ADMIN")
    void shouldCreateStudent() throws Exception {
        CreateStudentRequest req = new CreateStudentRequest(
                "John Doe", "john@test.com", "password123", "STU001", "CSE", 2026, null, null);

        mockMvc.perform(post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rollNumber").value("STU001"))
                .andExpect(jsonPath("$.branch").value("CSE"));
    }

    @Test
    @WithMockCampusUser(role = "STUDENT")
    void shouldFailCreateStudentWhenForbidden() throws Exception {
        CreateStudentRequest req = new CreateStudentRequest(
                "John Doe", "john@test.com", "password123", "STU001", "CSE", 2026, null, null);

        mockMvc.perform(post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCampusUser(role = "INSTITUTION_ADMIN")
    void shouldListStudents() throws Exception {
        mockMvc.perform(get("/api/v1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
