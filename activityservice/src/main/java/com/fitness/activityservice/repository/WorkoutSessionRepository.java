package com.fitness.activityservice.repository;

import com.fitness.activityservice.model.WorkoutSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkoutSessionRepository extends MongoRepository<WorkoutSession,String> {
    List<WorkoutSession> findByUserIdOrderByStartTimeDesc(String userId);
}
