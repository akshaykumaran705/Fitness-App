package com.fitness.activityservice.service;

import com.fitness.activityservice.dto.HeartRateReading;
import com.fitness.activityservice.dto.WorkoutSessionRequest;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.model.ActivityType;
import com.fitness.activityservice.model.WorkoutSession;
import com.fitness.activityservice.repository.ActivityRepository;
import com.fitness.activityservice.repository.WorkoutSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import scala.Int;

import java.time.Duration;
import java.util.List;

@Service
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final WorkoutSessionRepository workoutSessionRepository;

    @Autowired
    public ActivityService(ActivityRepository activityRepository,WorkoutSessionRepository workoutSessionRepository) {
        this.activityRepository = activityRepository;
        this.workoutSessionRepository = workoutSessionRepository;
    }
    public WorkoutSession saveWorkoutSession(String userId, WorkoutSessionRequest request){
        int durationSeconds = (int) Duration.between(request.getStartTime(), request.getEndTime()).getSeconds();
        Integer averageHeartRate = calculateAverageHeartRate(request.getHeartRateData());
        Integer maxHeartRate = calculateMaxHeartRate(request.getHeartRateData());
        Integer caloriesBurned = calculateCaloriesBurned(request.getActivityType(),durationSeconds);
        WorkoutSession session = WorkoutSession.builder()
                .userId(userId)
                .activityType(request.getActivityType())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationSeconds(durationSeconds)
                .distanceMeters(request.getDistanceMeters())
                .averageHeartRate(averageHeartRate)
                .maxHeartRate(maxHeartRate)
                .caloriesBurned(caloriesBurned)
                .route(request.getRoute())
                .heartRateData(request.getHeartRateData())
                .build();
        return workoutSessionRepository.save(session);
    }

    private Integer calculateCaloriesBurned(ActivityType activityType, int durationSeconds) {
        double caloriesPerMinute;
        switch (activityType) {
            case RUNNING : caloriesPerMinute = 10.0;break;
            case WALKING : caloriesPerMinute = 4.0; break;
            case CYCLING : caloriesPerMinute = 8.0; break;
            default : caloriesPerMinute = 5.0; break;
        }
        return (int) (caloriesPerMinute*durationSeconds/60.0);
    }

    private Integer calculateMaxHeartRate(List<HeartRateReading> heartRateData) {
        if(heartRateData == null || heartRateData.size() == 0){
            return null;
        }
        return heartRateData.stream()
                .mapToInt(HeartRateReading::getBpm)
                .max()
                .orElse(0);
    }

    private Integer calculateAverageHeartRate(List<HeartRateReading> heartRateData) {
        if(heartRateData == null || heartRateData.isEmpty()){
            return null;
        }
        return (int) heartRateData.stream()
                .mapToInt(HeartRateReading::getBpm)
                .average()
                .orElse(0.0);
    }
    public List<WorkoutSession> getWorkoutHistory(String userId) {
        return workoutSessionRepository.findByUserIdOrderByStartTimeDesc(userId);
    }
    public Page<Activity> getActivityHistory(String userId, int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return activityRepository.findActivitiesByUserIdOrderByStartTimeDesc(userId, pageable);
    }
    public Activity saveActivity(Activity activity) {
        return activityRepository.save(activity);
    }
}
