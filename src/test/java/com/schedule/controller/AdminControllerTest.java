package com.schedule.controller;

import com.schedule.model.*;
import com.schedule.repository.*;
import com.schedule.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;



@WebMvcTest(AdminController.class)
class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private TaskRepository taskRepository;
    @MockBean
    private EmailService emailService;
    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Create new user")
    void testCreateNewUser() throws Exception {
        when(passwordEncoder.encode(any())).thenReturn(UUID.randomUUID().toString());

        mockMvc.perform(post("/admin/create-user")
                .param("firstName", "Will")
                .param("lastName", "Smith")
                .param("email", "will.smith@testmail.com")
                .param("role","USER")
                        .with(csrf())
                        .with(user("john.doe@testmail.com").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/user-management"));

        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendNewUserEmail(anyString(),anyString(),contains("Password"));
    }

    @Test
    @DisplayName("Reset password")
    void testResetPassword() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("john.doe@testmail.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn(UUID.randomUUID().toString());

        mockMvc.perform(post("/admin/reset-password")
        .param("userId", "1")
                .param("newPassword", "newPassword123")
                        .with(csrf())
                        .with(user("john.doe@testmail.com").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/user-management"));

        verify(userRepository).save(user);
        verify(emailService).sendNewUserEmail(eq("john.doe@testmail.com"),anyString(),contains("newPassword123"));
    }

    @Test
    @DisplayName("Terminate user")
    void testTerminateUser() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("john.doe@testmail.com");

        Task task = new Task();
        task.setTaskId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.findByAssignedUser(user)).thenReturn(Collections.singletonList(task));

        mockMvc.perform(post("/admin/terminate-user")
        .param("userId", "1")
                        .with(csrf())
                        .with(user("john.doe@testmail.com").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/user-management"));

        verify(userRepository).save(user);
        verify(taskRepository).saveAll(anyList());
    }
}


