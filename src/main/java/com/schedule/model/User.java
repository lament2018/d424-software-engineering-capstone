package com.schedule.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Column(unique = true,nullable = false)
    @Email(message = "Please enter a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @NotBlank(message = "Password is required")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column(nullable=false)
    private boolean mustChangePassword = false;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Schedule> schedules;

    public enum Role{
        ADMIN,USER
    }
}