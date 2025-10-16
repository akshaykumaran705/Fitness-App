package com.fitness.activityservice.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SensorLoggerReading {
    private String name;  // "accelerometer", "gyroscope", etc.
    private Long time;    // nanoseconds
    private List<Double> values;  // x, y, z values
}
