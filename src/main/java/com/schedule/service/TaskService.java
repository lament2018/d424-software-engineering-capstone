package com.schedule.service;

import com.schedule.model.Schedule;
import com.schedule.model.Task;
import com.schedule.repository.ScheduleRepository;
import com.schedule.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final ScheduleRepository scheduleRepository;

    public TaskService(TaskRepository taskRepository, ScheduleRepository scheduleRepository) {
        this.taskRepository = taskRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public Task save(Task task) {
        return taskRepository.save(task);
    }
    public List<Task> getTaskBySchedule(Schedule schedule) {
        return taskRepository.findBySchedule(schedule);
    }

    public void updateScheduleStatus(Schedule schedule) {
        List<Task> tasks = schedule.getTasks();

        if(tasks.isEmpty()){
            schedule.setStatus(Schedule.Status.OPEN);
        }else{
            boolean allCompleted = tasks.stream().allMatch(t-> t.getStatus() == Task.Status.COMPLETED);
            boolean allOpen = tasks.stream().allMatch(t-> t.getStatus() == Task.Status.PENDING);
            boolean allCancelled = tasks.stream().allMatch(t-> t.getStatus() == Task.Status.CANCELLED);
            boolean allCompletedOrCancelled = tasks.stream().allMatch(task ->
                    task.getStatus() == Task.Status.COMPLETED || task.getStatus() == Task.Status.CANCELLED
            );

            if(allCompleted){
                schedule.setStatus(Schedule.Status.COMPLETED);
            }else if(allOpen){
                schedule.setStatus(Schedule.Status.OPEN);
            }else if(allCancelled){
                schedule.setStatus(Schedule.Status.CANCELLED);
            } else if (allCompletedOrCancelled) {
                schedule.setStatus(Schedule.Status.COMPLETED);
            } else{
                schedule.setStatus(Schedule.Status.PARTIALLY_COMPLETED);
            }
            scheduleRepository.save(schedule);
        }
    }
    public List<Task> search(String keyword,String priority, LocalDateTime dueBefore) {
        return taskRepository.findAll();
    }
}
