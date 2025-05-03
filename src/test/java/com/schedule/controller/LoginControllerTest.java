package com.schedule.controller;


import com.schedule.repository.TaskRepository;
import com.schedule.repository.UserRepository;
import com.schedule.controller.*;
import com.schedule.security.CustomAuthenticationSuccessHandler;
import com.schedule.security.CustomerDetailsService;
import com.schedule.service.UserService;
import com.schedule.service.EmailService;
import com.schedule.model.*;
import com.schedule.security.WebSecurityConfig;
import jakarta.persistence.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;


import java.util.Optional;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
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
        testUser.setRole(com.schedule.model.User.Role.ADMIN); // Make sure this matches your enum

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
}
