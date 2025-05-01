package com.schedule.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="user_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private Status status = Status.OPEN;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    private List<Task> tasks;

    public enum Status {
        OPEN,PARTIALLY_COMPLETED,COMPLETED,CANCELLED
    }
}
