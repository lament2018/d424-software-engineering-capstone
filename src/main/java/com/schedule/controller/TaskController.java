package com.schedule.controller;

import com.schedule.model.Schedule;
import com.schedule.model.Task;
import com.schedule.model.User;
import com.schedule.repository.ScheduleRepository;
import com.schedule.repository.TaskRepository;
import com.schedule.repository.UserRepository;
import com.schedule.service.TaskService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class TaskController {
    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public TaskController(TaskService taskService, TaskRepository taskRepository, ScheduleRepository scheduleRepository, UserRepository userRepository) {
        this.taskService = taskService;
        this.taskRepository = taskRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }
    @GetMapping("/task-management")
    public String viewAllTasks(Model model,Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email).orElseThrow();
        List<Task> tasks;
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;
        if (isAdmin) {
            tasks = taskRepository.findAll();
            model.addAttribute("users",userRepository.findAll());
        }else{
            tasks = taskRepository.findByAssignedUser(currentUser);
        }
        model.addAttribute("tasks", tasks);
        model.addAttribute("priorities", Task.Priority.values());
        model.addAttribute("statuses", Task.Status.values());
        //model.addAttribute("status", Schedule.Status.values());

        return "task-management";
    }
    @GetMapping("/schedules/{scheduleId}/tasks")
    public String viewTasks(@PathVariable Long scheduleId, Model model,
                            Authentication authentication) {
       try{
           User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
           if(user.getRole() != User.Role.ADMIN){
               return "redirect:/access-denied";
           }
           Schedule schedule = scheduleRepository.findById(scheduleId)
                   .orElseThrow(() -> new RuntimeException("Schedule not found for ID: " + scheduleId));

           List<Task> tasks = taskRepository.findBySchedule(schedule);
           model.addAttribute("schedule", schedule);
           model.addAttribute("tasks",tasks);
           model.addAttribute("priorities", Task.Priority.values());
           model.addAttribute("statuses", Task.Status.values());
           model.addAttribute("users", userRepository.findByActiveTrue());
        /*String email = authentication.getName();
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getRole() == User.Role.ADMIN) {
                model.addAttribute("users", userRepository.findAll());
                model.addAttribute("isAdmin", true);
            } else {
                model.addAttribute("isAdmin", false);
            }
        });*/
           /*System.out.println("Schedule ID: " + scheduleId);
           System.out.println("Schedule title: " + schedule.getTitle());
           System.out.println("Tasks found: " + tasks.size());
           System.out.println("Users count: " + userRepository.findAll().size());*/

           return "task-schedule";
       }catch(Exception e){
           e.printStackTrace();
           model.addAttribute("errorMessage", e.getMessage());
           return "error";
       }

    }

    @PostMapping("/schedules/{scheduleId}/tasks")
    public String createTask(@PathVariable Long scheduleId,
                             @RequestParam String title,
                             @RequestParam String description,
                             @RequestParam String priority,
                             @RequestParam Long assignedUserId,
                             @RequestParam String taskComment,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDate,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try{
            Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();
            Task.Priority priorityEnum = Task.Priority.valueOf(priority);
            User assignedUser = userRepository.findById(assignedUserId).orElse(null);

            //Validate due date
            if(dueDate.isBefore(schedule.getStartTime()) || dueDate.isAfter(schedule.getEndTime())){
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Due date must be between the schedule's start and end date.");
                return "redirect:/schedules/" + scheduleId + "/tasks";
            }

            Task task = new Task();
            task.setName(title);
            task.setDescription(description);
            task.setSchedule(schedule);
            task.setCreatedAt(LocalDateTime.now());
            task.setStatus(Task.Status.PENDING);
            task.setPriority(priorityEnum);
            task.setAssignedUser(assignedUser);
            task.setTaskComment(taskComment);
            task.setDueDate(dueDate);

            taskRepository.save(task);
            taskService.updateScheduleStatus(schedule);
            redirectAttributes.addFlashAttribute("successMessage", "Task created successfully.");

        }catch(Exception e){
            e.printStackTrace();
            model.addAttribute("errorMessage", e.getMessage());
            return "error";

        }
        return "redirect:/schedules/" + scheduleId + "/tasks";
    }

    @PostMapping("/update-task")
    public String updateTask(@RequestParam Long taskId,
                             @RequestParam String status,
                             @RequestParam String priority,
                             @RequestParam(required = false) String taskComment,
                             @RequestParam(required = false) Long assignedUserId,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             Authentication authentication) {
        Task task = new Task();
        try{
            task = taskRepository.findById(taskId).orElseThrow();
            //Prevent editing completed tasks
            if(task.getStatus() == Task.Status.COMPLETED) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot edit a completed task.");
                return "redirect:/schedules/" + task.getSchedule().getScheduleId() + "/tasks";
            }
            task.setStatus(Task.Status.valueOf(status));
            task.setPriority(Task.Priority.valueOf(priority));
            // Only allow ADMIN to reassign
            User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow();
            if (currentUser.getRole() == User.Role.ADMIN && assignedUserId != null) {
                task.setAssignedUser(userRepository.findById(assignedUserId).orElse(null));
            }

            //task.setAssignedUser(userRepository.findById(assignedUserId).orElse(null));
            task.setTaskComment(taskComment);
            task.setUpdatedAt(LocalDateTime.now());

            taskRepository.save(task);
            Schedule schedule = task.getSchedule();
            taskService.updateScheduleStatus(schedule);
            redirectAttributes.addFlashAttribute("successMessage", "Task is updated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            //redirectAttributes.addFlashAttribute("errorMessage", "Update task failed.");
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }

        return "redirect:/schedules/" + task.getSchedule().getScheduleId() + "/tasks";

    }

    @PostMapping("/update-task-management")
    public String updateTaskManagement(@RequestParam Long taskId,
                             @RequestParam String status,
                             @RequestParam String priority,
                             @RequestParam(required = false) String taskComment,
                             @RequestParam(required = false) Long assignedUserId,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             Authentication authentication) {
        Task task = new Task();
        try{
            task = taskRepository.findById(taskId).orElseThrow();
            //Prevent editing completed tasks
            if(task.getStatus() == Task.Status.COMPLETED) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot edit a completed task.");
                return "redirect:/task-management";
            }
            task.setStatus(Task.Status.valueOf(status));
            task.setPriority(Task.Priority.valueOf(priority));
            // Only allow ADMIN to reassign
            User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow();
            if (currentUser.getRole() == User.Role.ADMIN && assignedUserId != null) {
                task.setAssignedUser(userRepository.findById(assignedUserId).orElse(null));
            }

            //task.setAssignedUser(userRepository.findById(assignedUserId).orElse(null));
            task.setTaskComment(taskComment);
            task.setUpdatedAt(LocalDateTime.now());

            taskRepository.save(task);
            Schedule schedule = task.getSchedule();
            taskService.updateScheduleStatus(schedule);
            redirectAttributes.addFlashAttribute("successMessage", "Task is updated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            //redirectAttributes.addFlashAttribute("errorMessage", "Update task failed.");
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }

        return "redirect:/task-management";

    }
    @PostMapping("/delete-task")
    public String deleteTask(@RequestParam Long taskId,
                             @RequestParam(required = false) Long scheduleId,
                             RedirectAttributes redirectAttributes)
    {
        try{
            taskRepository.deleteById(taskId);
            redirectAttributes.addFlashAttribute("successMessage", "Task deleted successfully.");
        }catch(Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete the task. ");
        }
        if(scheduleId != null){
            return "redirect:/schedules/" + scheduleId + "/tasks";
        }else{
            return "redirect:/task-management";
        }

    }




}
