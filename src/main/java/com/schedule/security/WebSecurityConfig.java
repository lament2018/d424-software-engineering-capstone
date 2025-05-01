package com.schedule.security;

import com.schedule.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class WebSecurityConfig{
    private final UserRepository userRepository;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    public WebSecurityConfig(UserRepository userRepository,CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.userRepository = userRepository;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth  -> auth
                .requestMatchers("/","/home","/forgot-password","/css/**","/js/**").permitAll()
                        .requestMatchers("/register","/admin/**","/schedule-management","/schedules/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
        ).formLogin(form -> form.loginPage("/home")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/dashboard",true)
                        .failureUrl("/home?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout","GET"))
                        .logoutSuccessUrl("/home?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .exceptionHandling(ex->ex
                        .accessDeniedPage("/access-denied"));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}