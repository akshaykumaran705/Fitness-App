package com.fitness.userservice.dto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private String id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Integer dailyStepsGoal;
    private Integer dailyCaloriesGoal;
    private Integer dailyDistanceGoal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
