package com.fitness.activityservice.dto;

import lombok.Data;

@Data
public class GpsCoordinate {
    private double latitude;
    private double longitude;
    private long timestamp;
}
