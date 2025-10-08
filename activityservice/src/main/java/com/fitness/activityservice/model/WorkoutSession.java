package com.fitness.activityservice.model;

import com.fitness.activityservice.dto.GpsCoordinate;
import com.fitness.activityservice.dto.HeartRateReading;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import scala.Int;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "workout_sessions")
@Data
@Builder
public class WorkoutSession {
    private String id;
    private String userId;
    private ActivityType activityType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationSeconds;
    private Double distanceMeters;
    private Integer averageHeartRate;
    private Integer maxHeartRate;
    private Integer caloriesBurned;
    private List<GpsCoordinate> route;
    private List<HeartRateReading> heartRateData;
}
