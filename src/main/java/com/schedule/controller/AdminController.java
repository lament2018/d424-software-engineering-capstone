package com.schedule.controller;

import com.schedule.model.*;
import com.schedule.repository.TaskRepository;
import com.schedule.repository.UserRepository;
import com.schedule.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TaskRepository taskRepository;

    /*public AdminController(PasswordEncoder passwordEncoder, UserRepository userRepository, EmailService emailService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }*/
    @GetMapping("/user-management")
    public String userManagement(Model model, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        if(user.getRole() != User.Role.ADMIN){
            return "redirect:/access-denied";
        }
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "/admin/user-management";
    }
    @PostMapping("/create-user")
    public String createUser(@RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam String email,
                             @RequestParam User.Role role,
                             RedirectAttributes  redirectAttributes) {

        String rawPassword = generatePassword(8);
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User newUser = new User();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setPasswordHash(encodedPassword);
        newUser.setRole(role);
        try{
            userRepository.save(newUser);
            String subject = "Your Account Has Been Created";
            String message = "Hello " + firstName + " " + lastName+ ",\n\nYour account has been created.\nLogin: " + email + "\nPassword: " + rawPassword +
                    "\nPlease log in and change your password.";

            //System.out.println("Sending email to: " + email);
            emailService.sendNewUserEmail(email, subject, message);
            redirectAttributes.addFlashAttribute("successMessage","User has been created successfully.Email was sent out to the user.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Something went wrong.Please try again.");
        }

        return "redirect:/admin/user-management";
    }
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam Long userId,
                                @RequestParam String newPassword,
                                RedirectAttributes  redirectAttributes){
        User user = userRepository.findById(userId).orElseThrow();
        try{
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            emailService.sendNewUserEmail(user.getEmail(), "Password Reset",
                    "Your new password is: " + newPassword);
            redirectAttributes.addFlashAttribute("successMessage","Password has been updated. Email was sent out to the user.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Something went wrong.Please try again.");
        }


        return "redirect:/admin/user-management";
    }
    @PostMapping("/terminate-user")
    public String terminateUser(@RequestParam Long userId,
                                RedirectAttributes redirectAttributes){
        User user = userRepository.findById(userId).orElseThrow();

        try{
            //Mark user inactive
            user.setActive(false);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            //Reset tasks assigned to this user
            List<Task> assignedTasks = taskRepository.findByAssignedUser(user);
            for (Task task : assignedTasks) {
                task.setAssignedUser(null);
                task.setUpdatedAt(LocalDateTime.now());
            }
            taskRepository.saveAll(assignedTasks);
            redirectAttributes.addFlashAttribute("successMessage","User has been terminated and their tasks unassigned.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Something went wrong.Please try again.");
        }


        return "redirect:/admin/user-management";
    }

    private String generatePassword(int length) {
        return UUID.randomUUID().toString().substring(0, 8);
    }

}
