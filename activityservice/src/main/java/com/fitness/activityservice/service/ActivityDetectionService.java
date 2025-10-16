package com.fitness.activityservice.service;

import org.springframework.stereotype.Service;

@Service
public class ActivityDetectionService {
    
    private static final double IDLE_THRESHOLD = 9.5;
    private static final double WALKING_THRESHOLD = 11.0;
    private static final double RUNNING_THRESHOLD = 14.0;
    
    public String detectActivity(double magnitude) {
        if (magnitude < IDLE_THRESHOLD) return "IDLE";
        if (magnitude < WALKING_THRESHOLD) return "WALKING";
        if (magnitude < RUNNING_THRESHOLD) return "JOGGING";
        return "RUNNING";
    }
}