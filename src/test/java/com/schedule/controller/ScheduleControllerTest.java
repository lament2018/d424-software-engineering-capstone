package com.schedule.controller;

import com.schedule.model.*;
import com.schedule.repository.*;
import com.schedule.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;


@SpringBootTest
@AutoConfigureMockMvc
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private ScheduleRepository scheduleRepository;

    @MockBean
    private UserRepository userRepository;

    private User adminUser;

    @BeforeEach
    public void setUp() {
        adminUser = new User();
        adminUser.setUserId(1L);
        adminUser.setEmail("john.doe@testmail.com");
        adminUser.setFirstName("John");
        adminUser.setLastName("Doe");
        adminUser.setRole(User.Role.ADMIN);

        when(userRepository.findByEmail("john.doe@testmail.com")).thenReturn(Optional.of(adminUser));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(new Schedule()));
    }

    @Test
    @WithMockUser(username = "john.doe@testmail.com", roles = {"ADMIN"})
    void testCreateSchedule() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        mockMvc.perform(post("/create-schedule")
                .param("title","New Schedule")
                .param("description","New Schedule Description")
                .param("startTime",start.toString())
                .param("endTime",end.toString())
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedule-management"));
    }

    @Test
    @WithMockUser(username = "john.doe@testmail.com", roles = {"ADMIN"})
    void testUpdateSchedule() throws Exception {
        LocalDateTime updatedAt = LocalDateTime.now();
        mockMvc.perform(post("/update-schedule")
                .param("scheduleId","1")
                .param("newStatus","COMPLETED")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedule-management"));
    }

    @Test
    @WithMockUser(username = "john.doe@testmail.com", roles = {"ADMIN"})
    void testDeleteSchedule() throws Exception {
        mockMvc.perform(post("/delete-schedule")
                .param("scheduleId","1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedule-management"));
    }
}
