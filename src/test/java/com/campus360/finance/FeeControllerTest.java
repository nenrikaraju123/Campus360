package com.campus360.finance;

import com.campus360.testsupport.WithMockCampusUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class FeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockCampusUser(role = "INSTITUTION_ADMIN")
    void shouldListStructures() throws Exception {
        mockMvc.perform(get("/api/v1/finance/fees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCampusUser(role = "STUDENT")
    void shouldFailListStructuresWhenForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/finance/fees"))
                .andExpect(status().isForbidden());
    }
}
