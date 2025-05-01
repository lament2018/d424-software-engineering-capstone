package com.schedule.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="schedule_task")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @ManyToOne
    @JoinColumn(name="schedule_id")
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    private String name;
    private String description;
    private LocalDateTime dueDate;

    @Column(length = 1000)
    private String taskComment;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.LOW;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum Priority{
        LOW, MEDIUM, HIGH
    }
    public enum Status {
        PENDING,COMPLETED,CANCELLED
    }
}
