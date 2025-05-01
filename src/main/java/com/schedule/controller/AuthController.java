package com.schedule.controller;

import com.schedule.exception.DuplicateEmailException;
import com.schedule.repository.UserRepository;
import com.schedule.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.schedule.model.User;
import com.schedule.service.UserService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;


@Controller
public class AuthController {
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final EmailService emailService;
    //private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;

    }
    /*@GetMapping("/login")
    public String loginPage(){
        return "login";
    }*/

    @GetMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public String registerPage(Model model){
        model.addAttribute("user", new User());
        return "register";
    }
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public String processRegister(@ModelAttribute("user") @Valid User user,
                           BindingResult result,
                           Model model){
        if(result.hasErrors()){
            return "register";
        }
        try{
            userService.register(user);
            return "register-success";
        }catch (DuplicateEmailException e){
            model.addAttribute("error",e.getMessage());
            return "register";
        }catch (Exception e){
            model.addAttribute("error","Registration failed. Please try again.");
            return "register";
        }
        //return "redirect:/dashboard";
    }
    /*@GetMapping("/change-password")
    public String showChangeForm(){
        return "change-password";
    }*/
    /*@PostMapping("/change-password")
    public String changePassword(@RequestParam String newPassword, Principal principal){
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
        return "redirect:/change-password";
    }*/




    /*@PostMapping("/register")
    public String processRegister(@ModelAttribute("user") @Valid User user,
                                  BindingResult result,
                                  Model model){
        if(result.hasErrors()){
            return "register";
        }
        try{
            userService.register(user);
            return "register-success";
        }catch (DuplicateEmailException e){
            model.addAttribute("error",e.getMessage());
            return "register";
        }catch (Exception e){
            model.addAttribute("error","Registration failed. Please try again.");
            return "register";
        }
    }*/
    @GetMapping({"/","/home"})
    public String home(){
        return "home";
    }
    @GetMapping("/access-denied")
    public String accessDenied(){
        return "access-denied";
    }
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(){
        return "forgot-password";
    }
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email,
                                        RedirectAttributes redirectAttributes){

        System.out.println("Forgot password request for: " + email);
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "No user found with that email.");
                return "redirect:/forgot-password";
            }

            User user = userOpt.get();
            String newPassword = UUID.randomUUID().toString().substring(0, 8);
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            String message = "Your new password is: " + newPassword;

            try {
                emailService.sendNewUserEmail(user.getEmail(), "Password Reset", message);
                System.out.println("Would send email to " + user.getEmail());
            } catch (Exception ex) {
                System.out.println("Failed to send email:");
                ex.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to send reset email.");
                return "redirect:/forgot-password";
            }

            redirectAttributes.addFlashAttribute("successMessage", "Password reset email sent.");
            return "redirect:/home";

        } catch (Exception e) {
            System.out.println("Something else failed:");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error. Try again.");
            return "redirect:/forgot-password";
        }
    }
}
