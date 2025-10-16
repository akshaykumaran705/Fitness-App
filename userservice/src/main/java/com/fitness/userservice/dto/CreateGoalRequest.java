package com.fitness.userservice.dto;

import com.fitness.userservice.model.GoalType;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

// This class defines the exact shape of the incoming JSON request
@Data
public class CreateGoalRequest {
    private String title;
    private GoalType goalType;
    private Double targetValue;
    private Double currentValue;
    private Date startDate;
    private Date endDate;
}