package com.fitness.activityservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "sensor_sessions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SensorSession {
    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    private String sessionId;  // From Sensor Logger
    private String deviceId;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastUpdated;
    
    private List<ProcessedReading> readings;
    
    // Real-time metrics
    private Integer totalSteps;
    private Double totalDistance;  // meters
    private Double caloriesBurned;
    private String primaryActivity;
    
    private SessionStatus status;
}