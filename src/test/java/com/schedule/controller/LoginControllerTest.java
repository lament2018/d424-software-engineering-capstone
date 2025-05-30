package com.schedule.controller;


import com.schedule.repository.*;
import com.schedule.security.*;
import com.schedule.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;


import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;



@SpringBootTest
@AutoConfigureMockMvc
@Import(WebSecurityConfig.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private UserService userService;

    @MockBean
    private EmailService emailService;
    @MockBean
    private TaskRepository taskRepository;
    @MockBean
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;



    /*@TestConfiguration
    static class TestConfig{
        @Bean
        public PasswordEncoder passwordEncoder(){
            return new BCryptPasswordEncoder();
        }
    }*/

    @Test
    @DisplayName("Login Page - Should load login form")
    void shouldLoadLoginPage() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Login")));
    }

    @BeforeEach
    void setUp() {
        com.schedule.model.User testUser = new com.schedule.model.User();
        testUser.setEmail("john.doe@testmail.com");
        testUser.setPasswordHash(passwordEncoder.encode("Hercules#2011!"));
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(com.schedule.model.User.Role.ADMIN);

        when(userRepository.findByEmail("john.doe@testmail.com")).thenReturn(Optional.of(testUser));
        when(taskRepository.findByAssignedUser(any(com.schedule.model.User.class))).thenReturn(Collections.emptyList());
    }
    @Test
    @DisplayName("POST/login - Should redirect after successful login")
    void shouldRedirectAfterSuccessfulLogin() throws Exception {
        mockMvc.perform(post("/home")
                .param("email","john.doe@testmail.com")
                .param("password","Hercules#2011!")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("POST/login - Should redirect back to login on failure")
    void shouldRedirectAfterLoginOnFailure() throws Exception {
        mockMvc.perform(post("/home")
        .param("email","wronguser")
                .param("password","wrongpass")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home?error"));
    }

    @Test
    @DisplayName("Dashboard - Should allow access to authenticated user")
    void shouldAllowAccessToAuthenticatedUser() throws Exception {
        UserDetails mockUser = org.springframework.security.core.userdetails.User.builder()
                .username("john.doe@testmail.com")
                .password(passwordEncoder.encode("Hercules#2011!"))
                .roles("ADMIN")
                .build();

        //when(customerDetailsService.loadUserByUsername("john.doe@testmail.com")).thenReturn(mockUser);

        mockMvc.perform(get("/dashboard").with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(containsString("Dashboard")));
    }
    @Test
    @DisplayName("Dashboard - Should redirect unauthenticated user to login")
    void shouldRedirectUnauthenticatedUserToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/home"));
    }

    @Test
    @DisplayName("POST /forgot-password - should reset password and redirect to login with success message")
    void shouldProcessForgotPasswordSuccessfully() throws Exception {
        com.schedule.model.User testUser = new com.schedule.model.User();
        testUser.setEmail("john.doe@testmail.com");
        testUser.setPasswordHash(passwordEncoder.encode("Hercules#2011!"));
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        when(userRepository.findByEmail("john.doe@testmail.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/forgot-password")
        .param("email","john.doe@testmail.com")
        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(flash().attribute("successMessage", "Password reset email sent."));

    }

    @Test
    @DisplayName("GET /logout - should redirect to /home after logout")
    void shouldRedirectAfterLogout() throws Exception {
        mockMvc.perform(get("/logout")
        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home?logout"));
    }
}
