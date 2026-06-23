package com.campus360.academics;

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
public class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockCampusUser(role = "STUDENT", userId = 1L)
    void shouldGetStudentAttendance() throws Exception {
        mockMvc.perform(get("/api/v1/attendance/summary/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCampusUser(role = "STUDENT", userId = 2L)
    void shouldFailGetOtherStudentAttendance() throws Exception {
        mockMvc.perform(get("/api/v1/attendance/summary/1"))
                .andExpect(status().isNotFound());
    }
}
