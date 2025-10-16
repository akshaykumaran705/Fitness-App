package com.fitness.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "goals")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class Goal {
    @Id
    private String id;
    private String userId;
    private String title;
    @Enumerated(EnumType.STRING)
    private GoalType goalType;
    private double targetValue;
    private double currentValue;
    private Date startDate;
    @DateTimeFormat(pattern = "MM/dd/yyyy")
    private Date endDate;
    @Enumerated(EnumType.STRING)
    private GoalStatus status;

}
