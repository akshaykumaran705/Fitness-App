package com.fitness.activityservice.repository;

import com.fitness.activityservice.model.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityRepository extends MongoRepository<Activity, String> {
    Page<Activity> findActivitiesByUserIdOrderByStartTimeDesc(String userId, Pageable pageable);
    List<Activity> findActivitiesByUserIdAndStartTimeBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);
}
