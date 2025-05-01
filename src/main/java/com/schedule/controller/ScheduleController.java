package com.schedule.controller;

import com.schedule.model.Schedule;
import com.schedule.model.User;
import com.schedule.repository.ScheduleRepository;
import com.schedule.repository.UserRepository;
import com.schedule.service.ScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ScheduleController {
    private final ScheduleService scheduleService;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public ScheduleController(ScheduleService scheduleService, ScheduleRepository scheduleRepository, UserRepository userRepository) {
        this.scheduleService = scheduleService;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }
    @GetMapping("/schedule-management")
    public String listSchedules(Model model, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        if(user.getRole() != User.Role.ADMIN) {
            return "redirect:/access-denied";
        }
        model.addAttribute("schedules", scheduleRepository.findAll());
        //model.addAttribute("status", Schedule.Status.values());
        return "schedule-management";
    }
    @PostMapping("/create-schedule")
    public String createSchedule(@RequestParam String title,
                                 @RequestParam String description,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        //System.out.println("createSchedule() method called");
        try{
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElseThrow();

            Schedule schedule = new Schedule();
            schedule.setTitle(title);
            schedule.setDescription(description);
            schedule.setCreatedAt(LocalDateTime.now());
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);
            schedule.setStatus(Schedule.Status.OPEN);
            schedule.setUser(user);
            if(startTime.isAfter(endTime)){
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Start date must be before end date.");
                return "redirect:/schedule-management";
            } else if (endTime.isBefore( startTime)) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "End date must be after start date.");
                return "redirect:/schedule-management";
            }else if(startTime.isEqual(endTime)){
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Schedule Start and End date must be more than 24hours.");
                return "redirect:/schedule-management";
            } else if (startTime.isBefore(LocalDateTime.now()) || endTime.isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Schedule Start and End date must not be in the past.");
                return "redirect:/schedule-management";
            }
            //System.out.println("Saving schedule for: " + email);
            scheduleRepository.save(schedule);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }


        return "redirect:/schedule-management";

    }
    @PostMapping("/update-schedule")
    public String updateSchedule(@RequestParam Long scheduleId,
                                      @RequestParam Schedule.Status newStatus,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        if(user.getRole() != User.Role.ADMIN) {
            return "redirect:/access-denied";
        }
        try{
            Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();
            schedule.setStatus(newStatus);
            schedule.setUpdatedAt(LocalDateTime.now());
            scheduleRepository.save(schedule);

            redirectAttributes.addFlashAttribute("successMessage", "Schedule updated successfully!");
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Schedule updated failed!");
        }


        return "redirect:/schedule-management";
    }

    @PostMapping("delete-schedule")
    public String deleteSchedule(@RequestParam Long scheduleId,
                                 RedirectAttributes redirectAttributes) {
        try{
            scheduleRepository.deleteById(scheduleId);
            redirectAttributes.addFlashAttribute("successMessage", "Schedule deleted successfully!");
        }catch(Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "Schedule deletion failed!");
        }
        return "redirect:/schedule-management";
    }
}
