package com.schedule.service;

import com.schedule.exception.DuplicateEmailException;
import com.schedule.model.User;
import com.schedule.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;;

    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public List<User> findAll() {
        return userRepository.findAll();
    }
    public User save(User user) {
        return userRepository.save(user);
    }
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
    public User register(User user) {
        if(userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email is already in use");
        }
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }
}

