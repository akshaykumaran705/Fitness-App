package com.fitness.activityservice.dto;

import lombok.Data;

@Data
public class ActivityStatusResponse {
    private String lastDetectedActivity;
    private long timestamp;
    private String latestRecommendation;
}
