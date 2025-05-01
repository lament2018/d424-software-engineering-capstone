package com.schedule.controller;

import com.schedule.model.Task;
import com.schedule.repository.TaskRepository;
import com.schedule.repository.UserRepository;
import com.schedule.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class DashboardController {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public DashboardController(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal, Authentication authentication) {
        String email = principal.getName();
        /*String role = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");*/
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        LocalDateTime dueSoon = LocalDateTime.now().plusDays(3);


        model.addAttribute("tasksAssignedCount",taskRepository.countByAssignedUser(user));
        model.addAttribute("tasksDueCount", taskRepository.countByAssignedUserAndDueDateBefore(user,dueSoon));
        model.addAttribute("highPriorityCount",taskRepository.countByAssignedUserAndPriority(user, Task.Priority.HIGH));
        model.addAttribute("welcomeMessage", "Welcome back, " + user.getFirstName() + " " + user.getLastName() +  "!");
        //model.addAttribute("role", role);

        return "dashboard";
    }
}
