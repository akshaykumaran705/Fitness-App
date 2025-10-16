package com.fitness.activityservice.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SensorLoggerMessage {
    private Integer messageId;
    private String sessionId;
    private String deviceId;
    private List<SensorLoggerReading> payload;
}