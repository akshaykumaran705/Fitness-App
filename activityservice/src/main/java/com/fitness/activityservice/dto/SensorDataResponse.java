package com.fitness.activityservice.dto;

import lombok.Data;

@Data
public class SensorDataResponse {
    private long timestamp;
    private double acc_x;
    private double acc_y;
    private double acc_z;
    private double gyro_x;
    private double gyro_y;
    private double gyro_z;
}
