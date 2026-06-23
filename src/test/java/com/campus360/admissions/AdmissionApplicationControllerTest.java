package com.campus360.admissions;

import com.campus360.admissions.web.dto.AdmissionApplicationRequest;
import com.campus360.admissions.web.dto.WorkflowActionRequest;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AdmissionApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestTenantFactory tenantFactory;

    @Autowired
    private ObjectMapper objectMapper;

    private Long tenantId;

    @BeforeEach
    void setUp() {
        Institution tenant = tenantFactory.createTenant("Admission Test Uni");
        tenantId = tenant.getId();
    }

    // ---- Role Authorization ----

    @Test
    @WithMockCampusUser(role = "STUDENT")
    void studentCannotCreateApplication() throws Exception {
        AdmissionApplicationRequest req = buildRequest("candidate@test.com");
        mockMvc.perform(post("/api/v1/admissions/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCampusUser(role = "INSTITUTION_ADMIN")
    void adminCanCreateApplication() throws Exception {
        AdmissionApplicationRequest req = buildRequest("new.candidate@test.com");
        mockMvc.perform(post("/api/v1/admissions/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new.candidate@test.com"))
                .andExpect(jsonPath("$.status").value("APPLICATION_RECEIVED"))
                .andExpect(jsonPath("$.applicationNumber").isNotEmpty());
    }

    @Test
    @WithMockCampusUser(role = "INSTITUTION_ADMIN")
    void duplicateEmailShouldConflict() throws Exception {
        AdmissionApplicationRequest req = buildRequest("dup@test.com");
        mockMvc.perform(post("/api/v1/admissions/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // second request with same email
        mockMvc.perform(post("/api/v1/admissions/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockCampusUser(role = "INSTITUTION_ADMIN")
    void listApplicationsReturnsPagedResult() throws Exception {
        mockMvc.perform(get("/api/v1/admissions/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ---- Workflow Transitions ----

    @Test
    @WithMockCampusUser(role = "INSTITUTION_ADMIN")
    void workflowTransitionApproveFlow() throws Exception {
        // 1. Create
        AdmissionApplicationRequest req = buildRequest("flow.candidate@test.com");
        MvcResult result = mockMvc.perform(post("/api/v1/admissions/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // 2. Submit for review
        mockMvc.perform(post("/api/v1/admissions/applications/" + id + "/actions/submit-review")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));

        // 3. Shortlist
        mockMvc.perform(post("/api/v1/admissions/applications/" + id + "/actions/shortlist")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));

        // 4. Approve
        WorkflowActionRequest action = new WorkflowActionRequest();
        action.comment = "Meets all criteria";
        mockMvc.perform(post("/api/v1/admissions/applications/" + id + "/actions/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(action)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // 5. Status history should have 3 entries
        mockMvc.perform(get("/api/v1/admissions/applications/" + id + "/status-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @WithMockCampusUser(role = "INSTITUTION_ADMIN")
    void illegalTransitionShouldReturnBadRequest() throws Exception {
        AdmissionApplicationRequest req = buildRequest("illegal.trans@test.com");
        MvcResult result = mockMvc.perform(post("/api/v1/admissions/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Skipping intermediate steps — direct jump to enroll should be rejected
        mockMvc.perform(post("/api/v1/admissions/applications/" + id + "/actions/enroll")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ---- Helpers ----

    private AdmissionApplicationRequest buildRequest(String email) {
        AdmissionApplicationRequest req = new AdmissionApplicationRequest();
        req.firstName = "Test";
        req.lastName = "Candidate";
        req.email = email;
        req.phone = "9999999999";
        return req;
    }
}
