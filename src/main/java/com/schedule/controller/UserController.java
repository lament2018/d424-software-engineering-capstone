package com.schedule.controller;


import com.schedule.model.User;
import com.schedule.repository.UserRepository;
import com.schedule.service.TaskService;
import com.schedule.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
       this.userRepository = userRepository;
       this.passwordEncoder = passwordEncoder;
    }
    @GetMapping("/my-profile")
    public String viewProfile(Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        model.addAttribute("user", user);
        return "my-profile";
    }

    @PostMapping("/change-password")
    public String resetPassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                Authentication authentication,
                                RedirectAttributes  redirectAttributes) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        try{
            if(newPassword == null || newPassword.length() < 8){
                redirectAttributes.addFlashAttribute("errorMessage", "Password should be at least 8 characters");
                return "redirect:/my-profile";
            }
            if(!passwordEncoder.matches(currentPassword,user.getPasswordHash())){
                redirectAttributes.addFlashAttribute("errorMessage","Current password is incorrect.");
                return "redirect:/my-profile";
            }
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage","Password has been changed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Something went wrong.Please try again.");
        }
        return "redirect:/my-profile";
    }

}