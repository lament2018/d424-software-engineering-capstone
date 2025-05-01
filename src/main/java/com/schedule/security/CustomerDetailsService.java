package com.schedule.security;

import com.schedule.model.User;
import com.schedule.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomerDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomerDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Trying to load user: " + email);
        return userRepository.findByEmail(email)
                .map(user -> {
                    System.out.println("Found user: " + user.getEmail());
                    System.out.println("Stored password hash: " + user.getPasswordHash());
                    return org.springframework.security.core.userdetails.User
                            .withUsername(user.getEmail())
                            .password(user.getPasswordHash())
                            .roles(user.getRole().name())
                            .build();
                })
                .orElseThrow(() -> {
                    System.out.println("User not found: " + email);
                    return new UsernameNotFoundException("User not found");
                });
    }

    @PostConstruct
    public void init() {
        System.out.println("CustomerDetailsService initialized!");
    }

}
