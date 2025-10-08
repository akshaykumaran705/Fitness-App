package com.fitness.userservice.dto;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private Integer firstName;
    private Integer lastName;
    private Integer dailyStepsGoal;
    private Integer dailyCaloriesGoal;
    private Integer dailyDistanceGoal;
}
