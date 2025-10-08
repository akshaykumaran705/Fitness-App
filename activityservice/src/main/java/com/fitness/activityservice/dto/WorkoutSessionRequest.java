package com.fitness.activityservice.dto;

import com.fitness.activityservice.model.ActivityType;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class WorkoutSessionRequest {
private ActivityType activityType;
private LocalDateTime startTime;
private LocalDateTime endTime;
private Double distanceMeters;
private List<GpsCoordinate> route;
private List<HeartRateReading> heartRateData;

}
