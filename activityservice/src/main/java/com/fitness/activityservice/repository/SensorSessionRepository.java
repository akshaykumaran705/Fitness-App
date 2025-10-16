package com.fitness.activityservice.repository;

import com.fitness.activityservice.model.SensorSession;
import com.fitness.activityservice.model.SessionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SensorSessionRepository extends MongoRepository<SensorSession, String> {
    Optional<SensorSession> findByUserIdAndSessionIdAndStatus(
        String userId, String sessionId, SessionStatus status);
    
    List<SensorSession> findByUserIdOrderByStartTimeDesc(String userId);
    
    Optional<SensorSession> findBySessionIdAndStatus(String sessionId, SessionStatus status);
}