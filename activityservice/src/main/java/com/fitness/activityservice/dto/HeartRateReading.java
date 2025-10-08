package com.fitness.activityservice.dto;

import lombok.Data;

@Data
public class HeartRateReading {
    private int bpm;
    private long timestamp;
}
