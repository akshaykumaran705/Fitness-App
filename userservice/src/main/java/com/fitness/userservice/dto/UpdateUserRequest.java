package com.fitness.userservice.dto;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private Integer dailyStepsGoal;
    private Integer dailyCaloriesGoal;
    private Integer dailyDistanceGoal;
}
