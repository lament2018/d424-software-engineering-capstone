package com.schedule.controller;

import com.schedule.model.*;
import com.schedule.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;


@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskRepository taskRepository;

    @MockBean
    private ScheduleRepository scheduleRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        when(taskRepository.countByStatus(Task.Status.PENDING)).thenReturn(5L);
        when(taskRepository.countByStatus(Task.Status.CANCELLED)).thenReturn(3L);
        when(taskRepository.countByStatus(Task.Status.COMPLETED)).thenReturn(7L);

        com.schedule.model.User testUser1 = new com.schedule.model.User();
        testUser1.setUserId(1L);
        testUser1.setFirstName("John");
        testUser1.setLastName("Doe");

        com.schedule.model.User testUser2 = new com.schedule.model.User();
        testUser2.setUserId(2L);
        testUser2.setFirstName("Dennis");
        testUser2.setLastName("Hales");

        List<User> users = Arrays.asList(testUser1, testUser2);
        when(userRepository.findAll()).thenReturn(users);

        Task testTask1 = new Task();
        testTask1.setAssignedUser(testUser1);

        Task testTask2 = new Task();
        testTask2.setAssignedUser(testUser1);

        Task testTask3 = new Task();
        testTask3.setAssignedUser(testUser2);

        Task testTask4 = new Task();
        testTask4.setAssignedUser(testUser2);

        List<Task> allTasks = Arrays.asList(testTask1, testTask2, testTask3, testTask4);
        when(taskRepository.findAll()).thenReturn(allTasks);
    }

    @Test
    @DisplayName("GET/report-management - Should return report-management view")
    void shouldReturnReportManagementPage() throws Exception {
        mockMvc.perform(get("/report-management")
                        .with(user("john.doe@testmail.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("report-management"))
                .andExpect(content().string(containsString("Report Management")));

    }

    @Test
    @DisplayName("GET /reports/task-summary - should return task summary view")
    void shouldReturnTaskSummaryReport() throws Exception {
        mockMvc.perform(get("/reports/task-summary")
                        .with(user("john.doe@testmail.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/task-summary"))
                //.andExpect(model().attributeExists("taskStatusCount"))
                .andExpect(content().string(containsString("Task Summary")));
    }

    @Test
    @DisplayName("GET /reports/user-activity - should return user activity report view")
    void shouldReturnUserActivityReport() throws Exception {
        mockMvc.perform(get("/reports/user-activity")
                        .with(user("john.doe@testmail.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/user-activity"))
                .andExpect(content().string(containsString("User Activity")));
    }
}
