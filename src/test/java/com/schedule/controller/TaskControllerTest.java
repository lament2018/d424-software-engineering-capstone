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
class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private ScheduleRepository scheduleRepository;

    @MockBean
    private TaskRepository taskRepository;

    @MockBean
    private TaskService taskService;

    @MockBean
    private UserRepository userRepository;

    private User adminUser;
    private User regUser;
    private Schedule schedule;
    private Task task;
    private Task userTask;


    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setUserId(5L);
        adminUser.setEmail("john.doe@testmail.com");
        adminUser.setFirstName("John");
        adminUser.setLastName("Doe");
        adminUser.setRole(User.Role.ADMIN);

        schedule = new Schedule();
        schedule.setScheduleId(7L);
        schedule.setTitle("Schedule Title");
        schedule.setDescription("Schedule Description");
        schedule.setStartTime(LocalDateTime.now().plusDays(1));
        schedule.setEndTime(LocalDateTime.now().plusDays(2));

        task = new Task();
        task.setTaskId(13L);
        task.setName("Task Name");
        task.setDescription("Task Description");
        task.setStatus(Task.Status.PENDING);
        task.setPriority(Task.Priority.HIGH);
        task.setSchedule(schedule);
        task.setAssignedUser(adminUser);

        regUser = new User();
        regUser.setUserId(4L);
        regUser.setEmail("dennis.hales@testmail.com");
        regUser.setFirstName("Dennis");
        regUser.setLastName("Hales");
        regUser.setRole(User.Role.USER);

        userTask = new Task();
        userTask.setTaskId(14L);
        userTask.setAssignedUser(regUser);
        userTask.setSchedule(schedule);
    }

    @Test
    @WithMockUser(username = "john.doe@testmail.com",roles = {"ADMIN"})
    void getAllTasks() throws Exception {
        when(userRepository.findByEmail("john.doe@testmail.com")).thenReturn(Optional.of(adminUser));
        when(taskRepository.findAll()).thenReturn(List.of(task));
        when(userRepository.findAll()).thenReturn(List.of(adminUser));

        mockMvc.perform(get("/task-management"))
                .andExpect(status().isOk())
                .andExpect(view().name("task-management"))
                .andExpect(model().attributeExists("tasks"));
    }

    @Test
    @WithMockUser(username = "john.doe@testmail.com", roles = {"ADMIN"})
    void createTask() throws Exception {
        when(scheduleRepository.findById(7L)).thenReturn(Optional.of(schedule));
        when(userRepository.findById(5L)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(post("/schedules/7/tasks")
                .param("title", "Task Name")
                .param("description", "Task Description")
                .param("priority", "HIGH")
                .param("taskComment","None")
                .param("assignedUserId","5")
                .param("dueDate",LocalDateTime.now().plusDays(1).withNano(0).toString())
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedules/7/tasks"));
    }

    @Test
    @WithMockUser(username = "john.doe@testmail.com", roles = {"ADMIN"})
    void updateTask() throws Exception {
        task.setAssignedUser(adminUser);
        task.setSchedule(schedule);
        task.setStatus(Task.Status.PENDING);
        when(taskRepository.findById(13L)).thenReturn(Optional.of(task));
        when(userRepository.findById(5L)).thenReturn(Optional.of(adminUser));
        when(userRepository.findByEmail("john.doe@testmail.com")).thenReturn(Optional.of(adminUser));
        doNothing().when(taskService).updateScheduleStatus(schedule);


        mockMvc.perform(post("/update-task")
                .param("taskId", "13")
                .param("status", "PENDING")
                .param("priority", "HIGH")
                .param("taskComment","Updated")
                .param("assignedUserId","5")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedules/7/tasks"));
    }

    @Test
    @WithMockUser(username = "john.doe@testmail.com", roles = {"ADMIN"})
    void deleteTask() throws Exception {
        mockMvc.perform(post("/delete-task")
                .param("taskId", "13")
                .param("scheduleId", "7")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedules/7/tasks"));
    }

    @Test
    @WithMockUser(username = "dennis.hales@testmail.com", roles = {"USER"})
    void getTaskForUser() throws Exception {
        when(userRepository.findByEmail("dennis.hales@testmail.com")).thenReturn(Optional.of(regUser));
        when(taskRepository.findByAssignedUser(regUser)).thenReturn(List.of(userTask));

        mockMvc.perform(get("/task-management"))
                .andExpect(status().isOk())
                .andExpect(view().name("task-management"))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(model().attributeDoesNotExist("users"));
    }

}
