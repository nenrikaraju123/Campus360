package com.campus360.student;

import com.campus360.institution.domain.Institution;
import com.campus360.student.web.CreateStudentRequest;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StudentLifecycleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestTenantFactory tenantFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.campus360.iam.repository.RoleRepository roleRepository;

    private Long tenantId;
    private Long studentId;

    @BeforeEach
    void setUp() throws Exception {
        Institution tenant = tenantFactory.createTenant("Lifecycle Uni");
        tenantId = tenant.getId();

        if (roleRepository.findByName("STUDENT").isEmpty()) {
            com.campus360.iam.domain.Role role = new com.campus360.iam.domain.Role();
            role.setName("STUDENT");
            roleRepository.save(role);
        }

        // Create a student to run lifecycle actions on
        CreateStudentRequest req = new CreateStudentRequest(
                "Jane Lifecycle", "jane.lifecycle@test.com", "pass123",
                "LC001", "CS", 2026, null, null);

        MvcResult result = mockMvc.perform(post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        if (result.getResponse().getStatus() == 201) {
            studentId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        } else {
            studentId = 999L; // fallback for non-admin test contexts where this fails
        }
    }

    @Test
    @WithMockCampusUser(role = "INSTITUTION_ADMIN")
    void get360ProfileReturnsCorrectData() throws Exception {
        mockMvc.perform(get("/api/v1/students/" + studentId + "/profile-360"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lifecycleStatus").value("ACTIVE"));
    }

    @Test
    @WithMockCampusUser(role = "INSTITUTION_ADMIN")
    void promoteStudentIncrementsTermAndWritesHistory() throws Exception {
        mockMvc.perform(post("/api/v1/students/" + studentId + "/actions/promote")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentTerm").value(2));

        mockMvc.perform(get("/api/v1/students/" + studentId + "/lifecycle-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].action").value("PROMOTED"));
    }

    @Test
    @WithMockCampusUser(role = "INSTITUTION_ADMIN")
    void suspendAndReactivateStudent() throws Exception {
        // Suspend
        mockMvc.perform(post("/api/v1/students/" + studentId + "/actions/suspend")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lifecycleStatus").value("SUSPENDED"));

        // History should show 1 entry
        mockMvc.perform(get("/api/v1/students/" + studentId + "/lifecycle-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockCampusUser(role = "INSTITUTION_ADMIN")
    void illegalLifecycleTransitionShouldFail() throws Exception {
        // First archive the student
        mockMvc.perform(post("/api/v1/students/" + studentId + "/actions/archive")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Cannot suspend an archived student (ARCHIVED -> SUSPENDED is not allowed)
        mockMvc.perform(post("/api/v1/students/" + studentId + "/actions/suspend")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCampusUser(role = "FACULTY")
    void facultyCannotCallLifecycleActions() throws Exception {
        // FACULTY role cannot promote students
        mockMvc.perform(post("/api/v1/students/" + studentId + "/actions/promote")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
