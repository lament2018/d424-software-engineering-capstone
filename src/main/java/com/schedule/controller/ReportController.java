package com.schedule.controller;

import com.schedule.model.*;
import com.schedule.repository.ScheduleRepository;
import com.schedule.repository.TaskRepository;
import com.schedule.repository.UserRepository;
import org.springframework.boot.actuate.endpoint.web.Link;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ReportController {
    private final TaskRepository taskRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public ReportController(TaskRepository taskRepository, ScheduleRepository scheduleRepository,
                            UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }
    @GetMapping("/report-management")
    public String reportManagement(){
        return "report-management";
    }

    @GetMapping("/reports/task-summary")
    public String taskSummaryReport(Model model){
        Map<Task.Status,Long> taskStatusCount = new EnumMap<>(Task.Status.class);
        for(Task.Status status : Task.Status.values()){
            taskStatusCount.put(status, taskRepository.countByStatus(status));
        }
        model.addAttribute("taskStatusCount", taskStatusCount);
        return "reports/task-summary";
    }

    @GetMapping("/reports/user-activity")
    public String userActivityReport(Model model){
        //List<Object[]> result = scheduleRepository.countScheduleByCreatedDate();
        List<User> users = userRepository.findAll();
        Map<Long,Long> taskCounts = taskRepository.findAll().stream()
                .filter(task -> task.getAssignedUser() != null)
                .collect(Collectors.groupingBy(task -> task.getAssignedUser().getUserId(), Collectors.counting()));



        model.addAttribute("users", users);
        model.addAttribute("taskCounts", taskCounts);
        return "reports/user-activity";
    }
}
