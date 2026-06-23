package com.campus360.placement;

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
public class PlacementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockCampusUser(role = "STUDENT")
    void shouldListPostingsForStudent() throws Exception {
        mockMvc.perform(get("/api/v1/placements/postings"))
                .andExpect(status().isOk());
    }
}
