package com.schedule.controller;

import com.schedule.model.User;
import com.schedule.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {
    @Autowired
    private UserRepository userRepository;

    @ModelAttribute
    public void addUserInitials(Model model,Authentication authentication) {
        if(authentication != null && authentication.isAuthenticated()){
            String email = authentication.getName();
            userRepository.findByEmail(email).ifPresent(user -> {
                String initials = user.getFirstName().substring(0,1).toUpperCase()
                        + user.getLastName().substring(0,1).toUpperCase();
                model.addAttribute("userInitials", initials);
                model.addAttribute("role", user.getRole().name());
            });
        }
    }
}
