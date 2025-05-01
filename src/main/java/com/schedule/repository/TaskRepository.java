package com.schedule.repository;

import com.schedule.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findBySchedule(Schedule schedule);

    List<Task> findByAssignedUser(User currentUser);

    long countByAssignedUser(User user);
    long countByAssignedUserAndDueDateBefore(User user,LocalDateTime dueDate);
    long countByAssignedUserAndPriority(User user, Task.Priority priority);
    long countByStatus(Task.Status status);

}
