package com.fitness.userservice;

import com.fitness.userservice.model.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false,unique = true)
    private String fireBaseUid;
    @Column(nullable = false, unique = true)
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Integer dailyStepGoals;
    private Integer dailyCaloriesGoal;
    private Integer dailyDistanceGoal;
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
