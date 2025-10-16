package com.fitness.activityservice.model;

import lombok.Data;

@Data
public class ProcessedReading {
    private Long timestamp;  // milliseconds
    
    private Double accX;
    private Double accY;
    private Double accZ;
    private Double accMagnitude;
    
    private Double gyroX;
    private Double gyroY;
    private Double gyroZ;
    
    private String activity;
    private Boolean isStep;
}