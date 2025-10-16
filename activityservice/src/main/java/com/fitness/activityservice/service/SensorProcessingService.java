package com.fitness.activityservice.service;

import com.fitness.activityservice.dto.*;
import com.fitness.activityservice.model.*;
import com.fitness.activityservice.repository.SensorSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensorProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(SensorProcessingService.class);
    
    private final SensorSessionRepository sessionRepository;
    private final ActivityDetectionService activityDetector;
    private final StepCounterService stepCounter;
    private final HarService harService;  // For ML-based activity detection
    
    /**
     * Process incoming Sensor Logger message and convert to HAR format
     */
    public void processSensorLoggerMessage(String userId, SensorLoggerMessage message) {
        logger.info("📱 Received message {} from session {} for user {}", 
            message.getMessageId(), message.getSessionId(), userId);
        
        // Get or create session
        SensorSession session = getOrCreateSession(userId, message.getSessionId(), message.getDeviceId());
        
        // Group readings by timestamp
        Map<Long, Map<String, Map<String, Object>>> groupedByTime = groupByTimestamp(message.getPayload());
        
        // Process each timestamp
        List<ProcessedReading> newReadings = new ArrayList<>();
        List<SensorDataResponse> harFormatReadings = new ArrayList<>();
        
        for (Map.Entry<Long, Map<String, Map<String, Object>>> entry : groupedByTime.entrySet()) {
            Long timestampMs = entry.getKey();
            Map<String, Map<String, Object>> sensors = entry.getValue();
            
            // Create processed reading
            ProcessedReading reading = processTimestamp(timestampMs, sensors);
            if (reading != null) {
                newReadings.add(reading);
                
                // Also create HAR format for ML model
                SensorDataResponse harReading = new SensorDataResponse();
                harReading.setTimestamp(timestampMs);
                harReading.setAcc_x(reading.getAccX());
                harReading.setAcc_y(reading.getAccY());
                harReading.setAcc_z(reading.getAccZ());
                harReading.setGyro_x(reading.getGyroX());
                harReading.setGyro_y(reading.getGyroY());
                harReading.setGyro_z(reading.getGyroZ());
                harFormatReadings.add(harReading);
            }
        }
        
        // Add to session
        if (session.getReadings() == null) {
            session.setReadings(new ArrayList<>());
        }
        session.getReadings().addAll(newReadings);
        
        // Update metrics
        updateMetrics(session, newReadings);
        
        // Save session
        session.setLastUpdated(LocalDateTime.now());
        sessionRepository.save(session);
        
        // Send to ML model for activity recognition
        if (!harFormatReadings.isEmpty()) {
            harService.processIncomingData(userId, harFormatReadings);
        }
        
        logger.info("✅ Processed {} readings | Steps: {} | Distance: {:.2f}m | Activity: {}", 
            newReadings.size(), 
            session.getTotalSteps(), 
            session.getTotalDistance(),
            session.getPrimaryActivity());
    }
    
    /**
     * Group sensor readings by timestamp (convert ns to ms)
     */
    private Map<Long, Map<String, Map<String, Object>>> groupByTimestamp(
        List<SensorLoggerReading> readings) {

    Map<Long, Map<String, Map<String, Object>>> grouped = new HashMap<>();

    for (SensorLoggerReading reading : readings) {
        if (!isSensorType(reading.getName())) continue;

        // Convert nanoseconds to milliseconds
        Long timestampMs = reading.getTime() / 1_000_000;
        String sensorType = normalizeSensorName(reading.getName());

        // For this timestamp, get or create a nested sensorType map
        grouped.computeIfAbsent(timestampMs, k -> new HashMap<>())
               .put(sensorType, Map.of("values", reading.getValues()));
    }

    return grouped;
}
    
    private boolean isSensorType(String name) {
        return name.contains("accelerometer") || 
               name.contains("gyroscope") ||
               name.contains("magnetometer");
    }
    
    private String normalizeSensorName(String name) {
        if (name.contains("accelerometer")) return "accelerometer";
        if (name.contains("gyroscope")) return "gyroscope";
        if (name.contains("magnetometer")) return "magnetometer";
        return name;
    }
    
    private ProcessedReading processTimestamp(Long timestampMs, 
                                              Map<String, Map<String, Object>> sensors) {
        
        ProcessedReading reading = new ProcessedReading();
        reading.setTimestamp(timestampMs);
        
        // Extract accelerometer
        Map<String, Object> acc = sensors.get("accelerometer");
        if (acc != null) {
            reading.setAccX(getDoubleValue(acc, "x"));
            reading.setAccY(getDoubleValue(acc, "y"));
            reading.setAccZ(getDoubleValue(acc, "z"));
            
            double magnitude = Math.sqrt(
                Math.pow(reading.getAccX(), 2) +
                Math.pow(reading.getAccY(), 2) +
                Math.pow(reading.getAccZ(), 2)
            );
            reading.setAccMagnitude(magnitude);
            reading.setActivity(activityDetector.detectActivity(magnitude));
        }
        
        // Extract gyroscope
        Map<String, Object> gyro = sensors.get("gyroscope");
        if (gyro != null) {
            reading.setGyroX(getDoubleValue(gyro, "x"));
            reading.setGyroY(getDoubleValue(gyro, "y"));
            reading.setGyroZ(getDoubleValue(gyro, "z"));
        }
        
        return reading;
    }
    
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
    
    private void updateMetrics(SensorSession session, List<ProcessedReading> newReadings) {
        // Count steps
        int newSteps = stepCounter.countSteps(newReadings);
        session.setTotalSteps((session.getTotalSteps() != null ? session.getTotalSteps() : 0) + newSteps);
        
        // Calculate distance
        session.setTotalDistance(session.getTotalSteps() * 0.762);
        
        // Estimate calories
        session.setCaloriesBurned(session.getTotalSteps() * 0.04);
        
        // Determine primary activity
        Map<String, Long> activityCounts = newReadings.stream()
            .filter(r -> r.getActivity() != null)
            .collect(Collectors.groupingBy(ProcessedReading::getActivity, Collectors.counting()));
        
        if (!activityCounts.isEmpty()) {
            session.setPrimaryActivity(
                activityCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("IDLE")
            );
        }
    }
    
    private SensorSession getOrCreateSession(String userId, String sessionId, String deviceId) {
        return sessionRepository.findByUserIdAndSessionIdAndStatus(userId, sessionId, SessionStatus.ACTIVE)
            .orElseGet(() -> {
                logger.info("🆕 Creating new sensor session for user {}", userId);
                SensorSession session = SensorSession.builder()
                    .userId(userId)
                    .sessionId(sessionId)
                    .deviceId(deviceId)
                    .startTime(LocalDateTime.now())
                    .status(SessionStatus.ACTIVE)
                    .totalSteps(0)
                    .totalDistance(0.0)
                    .caloriesBurned(0.0)
                    .build();
                return sessionRepository.save(session);
            });
    }
    
    public SensorSession endSession(String userId, String sessionId) {
        Optional<SensorSession> session = sessionRepository
            .findByUserIdAndSessionIdAndStatus(userId, sessionId, SessionStatus.ACTIVE);
        
        if (session.isPresent()) {
            SensorSession s = session.get();
            s.setStatus(SessionStatus.COMPLETED);
            s.setEndTime(LocalDateTime.now());
            logger.info("🛑 Ended session {} for user {}", sessionId, userId);
            return sessionRepository.save(s);
        }
        return null;
    }
    
    public List<SensorSession> getUserSessions(String userId) {
        return sessionRepository.findByUserIdOrderByStartTimeDesc(userId);
    }
}